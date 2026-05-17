package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.entity.Asset;
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
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final GoodItemRepository goodItemRepository;
    private final AssetRepository assetRepository;
    private final SystemConfigService systemConfigService;
    private final WebhookService webhookService;

    public Page<Notification> list(Pageable pageable) {
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

    private List<Notification> checkItemExpiry() {
        List<Notification> created = new ArrayList<>();
        List<GoodItem> items = goodItemRepository.findInUseItemsOrderByExpirationAsc(
                org.springframework.data.domain.Pageable.unpaged());

        LocalDate today = LocalDate.now();

        for (GoodItem item : items) {
            int expiringSoonDays = item.getGood().getExpiringSoonDays();
            long daysUntil = ChronoUnit.DAYS.between(today, item.getExpirationDate());

            NotificationType type;
            String title;
            String content;

            if (daysUntil < 0) {
                type = NotificationType.ITEM_EXPIRED;
                title = "❗ 物品已过期";
                content = String.format("【%s-%s】已于 %s 过期",
                        item.getGood().getBrand().getBrandName(),
                        item.getGood().getProductName(),
                        item.getExpirationDate());
            } else if (daysUntil <= expiringSoonDays) {
                type = NotificationType.ITEM_EXPIRING;
                title = "⚠️ 物品即将过期";
                content = String.format("【%s-%s】将于 %s 过期（剩余 %d 天）",
                        item.getGood().getBrand().getBrandName(),
                        item.getGood().getProductName(),
                        item.getExpirationDate(),
                        daysUntil);
            } else {
                continue;
            }

            if (notificationRepository.existsByTypeAndTitleAndContent(type, title, content)) {
                continue;
            }

            Notification notification = new Notification();
            notification.setType(type);
            notification.setTitle(title);
            notification.setContent(content);
            created.add(notificationRepository.save(notification));
        }

        return created;
    }

    private List<Notification> checkAssetWarranty() {
        List<Notification> created = new ArrayList<>();
        int expiringSoonDays = getAssetExpiringSoonDays();

        List<Asset> assets = assetRepository.findAll().stream()
                .filter(a -> a.isInUse() && a.isHasWarranty() && a.getExpirationDate() != null)
                .toList();

        LocalDate today = LocalDate.now();

        for (Asset asset : assets) {
            long daysUntil = ChronoUnit.DAYS.between(today, asset.getExpirationDate());

            NotificationType type;
            String title;
            String content;

            if (daysUntil < 0) {
                type = NotificationType.WARRANTY_EXPIRED;
                title = "❗ 资产保修已过期";
                content = String.format("【%s】的保修期已于 %s 到期",
                        asset.getName(),
                        asset.getExpirationDate());
            } else if (daysUntil <= expiringSoonDays) {
                type = NotificationType.WARRANTY_EXPIRING;
                title = "⚠️ 资产保修即将到期";
                content = String.format("【%s】的保修期将于 %s 到期（剩余 %d 天）",
                        asset.getName(),
                        asset.getExpirationDate(),
                        daysUntil);
            } else {
                continue;
            }

            if (notificationRepository.existsByTypeAndTitleAndContent(type, title, content)) {
                continue;
            }

            Notification notification = new Notification();
            notification.setType(type);
            notification.setTitle(title);
            notification.setContent(content);
            created.add(notificationRepository.save(notification));
        }

        return created;
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
