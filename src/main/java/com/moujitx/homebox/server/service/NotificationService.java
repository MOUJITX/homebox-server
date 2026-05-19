package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.entity.Asset;
import com.moujitx.homebox.server.entity.Good;
import com.moujitx.homebox.server.entity.GoodItem;
import com.moujitx.homebox.server.entity.Notification;
import com.moujitx.homebox.server.enums.NotificationType;
import com.moujitx.homebox.server.repository.AssetRepository;
import com.moujitx.homebox.server.repository.GoodItemRepository;
import com.moujitx.homebox.server.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String SOURCE_TYPE_GOOD = "GOOD";
    private static final String SOURCE_TYPE_ASSET = "ASSET";

    private final NotificationRepository notificationRepository;
    private final GoodItemRepository goodItemRepository;
    private final AssetRepository assetRepository;
    private final SystemConfigService systemConfigService;
    private final WebhookService webhookService;

    public Page<Notification> list(Pageable pageable, Boolean isRead) {
        if (isRead != null) {
            return notificationRepository.findByIsReadOrderByCreatedAtDesc(isRead, pageable);
        }
        return notificationRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public long getUnreadCount() {
        return notificationRepository.countByIsReadFalse();
    }

    @Transactional
    public boolean markRead(Long id) {
        return notificationRepository.findById(id).map(n -> {
            if (!n.isRead()) {
                n.setRead(true);
                n.setReadAt(LocalDateTime.now());
                notificationRepository.save(n);
                return true;
            }
            return false;
        }).orElse(false);
    }

    @Transactional
    public int markAllRead() {
        List<Notification> unread = notificationRepository.findUnread(Pageable.unpaged());
        for (Notification n : unread) {
            n.setRead(true);
            n.setReadAt(LocalDateTime.now());
        }
        notificationRepository.saveAll(unread);
        return unread.size();
    }

    @Transactional
    public void checkAndNotify() {
        log.info("Running expiry notification check");
        List<Notification> newNotifications = new ArrayList<>();
        newNotifications.addAll(checkItemExpiry());
        newNotifications.addAll(checkAssetWarranty());

        if (!newNotifications.isEmpty()) {
            log.info("Created {} new notifications", newNotifications.size());
            for (Notification notification : newNotifications) {
                webhookService.send(notification);
            }
        }
    }

    // ────────────────────────── Item Expiry ──────────────────────────

    private List<Notification> checkItemExpiry() {
        List<Notification> created = new ArrayList<>();
        List<GoodItem> items = goodItemRepository.findAllInUseWithGood();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Group by Good → expirationDate → items
        Map<Good, Map<LocalDate, List<GoodItem>>> grouped = items.stream()
                .collect(Collectors.groupingBy(
                        GoodItem::getGood,
                        LinkedHashMap::new,
                        Collectors.groupingBy(GoodItem::getExpirationDate, LinkedHashMap::new, Collectors.toList())
                ));

        for (var goodEntry : grouped.entrySet()) {
            Good good = goodEntry.getKey();
            Map<LocalDate, List<GoodItem>> dateGroups = goodEntry.getValue();
            int expiringSoonDays = good.getExpiringSoonDays();

            // --- Expiring: expirationDate ∈ [today, today + expiringSoonDays] ---
            Map<LocalDate, Long> expiringCounts = new LinkedHashMap<>();
            for (var dateEntry : dateGroups.entrySet()) {
                LocalDate expirationDate = dateEntry.getKey();
                long daysUntil = ChronoUnit.DAYS.between(today, expirationDate);
                if (daysUntil >= 0 && daysUntil <= expiringSoonDays) {
                    expiringCounts.put(expirationDate, (long) dateEntry.getValue().size());
                }
            }
            if (!expiringCounts.isEmpty()) {
                String title = "⚠️ 物品即将过期";
                String content = buildItemExpiringContent(good, expiringCounts, today);
                tryInsert(SOURCE_TYPE_GOOD, good.getId(), NotificationType.ITEM_EXPIRING, title, content, today, created);
            }

            // --- Expired: expirationDate == yesterday ---
            Map<LocalDate, Long> expiredCounts = new LinkedHashMap<>();
            for (var dateEntry : dateGroups.entrySet()) {
                LocalDate expirationDate = dateEntry.getKey();
                if (expirationDate.equals(yesterday)) {
                    expiredCounts.put(expirationDate, (long) dateEntry.getValue().size());
                }
            }
            if (!expiredCounts.isEmpty()) {
                String title = "❗ 物品已过期";
                String content = buildItemExpiredContent(good, expiredCounts);
                for (var expiredEntry : expiredCounts.entrySet()) {
                    tryInsert(SOURCE_TYPE_GOOD, good.getId(), NotificationType.ITEM_EXPIRED,
                            title, content, expiredEntry.getKey(), created);
                }
            }
        }

        return created;
    }

    private String buildItemExpiringContent(Good good, Map<LocalDate, Long> dateCounts, LocalDate today) {
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(good.getBrand().getBrandName())
                .append("-").append(good.getProductName()).append("】");

        List<Map.Entry<LocalDate, Long>> sorted = new ArrayList<>(dateCounts.entrySet());
        sorted.sort(Map.Entry.comparingByKey());

        if (sorted.size() == 1) {
            var entry = sorted.get(0);
            long daysUntil = ChronoUnit.DAYS.between(today, entry.getKey());
            sb.append("将于 ").append(entry.getKey())
                    .append(" 到期（剩余 ").append(daysUntil).append(" 天），共 ")
                    .append(entry.getValue()).append(" 件");
        } else {
            for (int i = 0; i < sorted.size(); i++) {
                if (i > 0) {
                    sb.append("，");
                }
                var entry = sorted.get(i);
                long daysUntil = ChronoUnit.DAYS.between(today, entry.getKey());
                sb.append("有 ").append(entry.getValue()).append(" 件将于 ")
                        .append(entry.getKey()).append(" 到期（剩余 ").append(daysUntil).append(" 天）");
            }
        }

        return sb.toString();
    }

    private String buildItemExpiredContent(Good good, Map<LocalDate, Long> dateCounts) {
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(good.getBrand().getBrandName())
                .append("-").append(good.getProductName()).append("】");

        List<Map.Entry<LocalDate, Long>> sorted = new ArrayList<>(dateCounts.entrySet());
        sorted.sort(Map.Entry.comparingByKey());

        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0) {
                sb.append("；");
            }
            var entry = sorted.get(i);
            sb.append("已于 ").append(entry.getKey())
                    .append(" 到期，共 ").append(entry.getValue()).append(" 件");
        }

        return sb.toString();
    }

    // ────────────────────────── Asset Warranty ──────────────────────────

    private List<Notification> checkAssetWarranty() {
        List<Notification> created = new ArrayList<>();
        int expiringSoonDays = getAssetExpiringSoonDays();

        List<Asset> assets = assetRepository.findAll().stream()
                .filter(a -> a.isInUse() && a.isHasWarranty() && a.getExpirationDate() != null)
                .toList();

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        for (Asset asset : assets) {
            long daysUntil = ChronoUnit.DAYS.between(today, asset.getExpirationDate());

            // Expiring: expirationDate ∈ [today, today + expiringSoonDays]
            if (daysUntil >= 0 && daysUntil <= expiringSoonDays) {
                String title = "⚠️ 资产保修即将到期";
                String content = buildAssetExpiringContent(asset, daysUntil);
                tryInsert(SOURCE_TYPE_ASSET, asset.getId(), NotificationType.WARRANTY_EXPIRING,
                        title, content, today, created);
            } else if (asset.getExpirationDate().equals(yesterday)) {
                // Expired: expirationDate == yesterday
                String title = "❗ 资产保修已过期";
                String content = buildAssetExpiredContent(asset);
                tryInsert(SOURCE_TYPE_ASSET, asset.getId(), NotificationType.WARRANTY_EXPIRED,
                        title, content, asset.getExpirationDate(), created);
            }
        }

        return created;
    }

    private String buildAssetExpiringContent(Asset asset, long daysUntil) {
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(asset.getName()).append("】");
        if (asset.getSerialNumber() != null && !asset.getSerialNumber().isEmpty()) {
            sb.append("(SN: ").append(asset.getSerialNumber()).append(")");
        }
        sb.append("的保修期将于 ").append(asset.getExpirationDate())
                .append(" 到期（剩余 ").append(daysUntil).append(" 天）");
        return sb.toString();
    }

    private String buildAssetExpiredContent(Asset asset) {
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(asset.getName()).append("】");
        if (asset.getSerialNumber() != null && !asset.getSerialNumber().isEmpty()) {
            sb.append("(SN: ").append(asset.getSerialNumber()).append(")");
        }
        sb.append("的保修期已于 ").append(asset.getExpirationDate()).append(" 到期");
        return sb.toString();
    }

    // ────────────────────────── Helpers ────────────────────────────────

    private void tryInsert(String sourceType, Long sourceId, NotificationType type,
                           String title, String content, LocalDate notifyDate,
                           List<Notification> created) {
        notificationRepository.insert(
                type.name(), title, content, sourceType, sourceId, notifyDate);
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setSourceType(sourceType);
        notification.setSourceId(sourceId);
        notification.setNotifyDate(notifyDate);
        notification.setCreatedAt(LocalDateTime.now());
        created.add(notification);
    }

    private int getAssetExpiringSoonDays() {
        String val = systemConfigService.get("notification.asset-expiring-soon-days");
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 30;
        }
    }
}
