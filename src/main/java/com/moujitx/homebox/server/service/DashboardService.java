package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.response.DashboardResponse;
import com.moujitx.homebox.server.entity.Asset;
import com.moujitx.homebox.server.entity.GoodItem;
import com.moujitx.homebox.server.entity.Subscription;
import com.moujitx.homebox.server.entity.SubscriptionRecord;
import com.moujitx.homebox.server.repository.AssetRepository;
import com.moujitx.homebox.server.repository.GoodItemRepository;
import com.moujitx.homebox.server.repository.InvoiceRepository;
import com.moujitx.homebox.server.repository.SubscriptionRecordRepository;
import com.moujitx.homebox.server.repository.SubscriptionRepository;
import com.moujitx.homebox.server.util.OssUrlBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final GoodItemRepository goodItemRepository;
    private final AssetRepository assetRepository;
    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionRecordRepository recordRepository;
    private final AssetService assetService;

    private static final int DASHBOARD_LIST_LIMIT = 10;
    private static final int UPCOMING_RENEWAL_DAYS = 7;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        DashboardResponse.Stats stats = buildStats();
        List<DashboardResponse.ItemSummary> expiringSoonItems = buildExpiringSoonItems();
        List<DashboardResponse.ItemSummary> inUseItems = buildInUseItems();
        List<DashboardResponse.WarrantyExpiringAsset> warrantyExpiringAssets = buildWarrantyExpiringAssets();
        List<DashboardResponse.InUseAsset> inUseAssets = buildInUseAssets();
        List<DashboardResponse.UpcomingRenewal> upcomingRenewals = buildUpcomingRenewals();

        return new DashboardResponse(stats, expiringSoonItems, inUseItems, warrantyExpiringAssets, inUseAssets, upcomingRenewals);
    }

    private DashboardResponse.Stats buildStats() {
        long itemCount = goodItemRepository.countInUseItems();
        long assetCount = assetRepository.count();
        var totalAssetPrice = assetRepository.sumAllPrices();
        long invoiceCount = invoiceRepository.count();
        long activeSubscriptionCount = subscriptionRepository.countActive();
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        BigDecimal monthlySubscriptionSpending = subscriptionRepository.sumAmountByDateRange(monthStart, monthEnd);
        return new DashboardResponse.Stats(itemCount, assetCount, totalAssetPrice, invoiceCount,
                activeSubscriptionCount, monthlySubscriptionSpending);
    }

    private List<DashboardResponse.ItemSummary> buildExpiringSoonItems() {
        // Fetch more items than needed so we can post-filter by per-good expiringSoonDays
        List<GoodItem> candidates = goodItemRepository.findInUseItemsOrderByExpirationAsc(
                PageRequest.of(0, DASHBOARD_LIST_LIMIT * 3));

        LocalDate today = LocalDate.now();

        return candidates.stream()
                .filter(item -> {
                    int expiringSoonDays = item.getGood().getExpiringSoonDays();
                    if (item.getExpirationDate().isBefore(today)) return false;
                    long daysUntil = ChronoUnit.DAYS.between(today, item.getExpirationDate());
                    return daysUntil <= expiringSoonDays;
                })
                .sorted(Comparator.comparing(GoodItem::getExpirationDate))
                .limit(DASHBOARD_LIST_LIMIT)
                .map(DashboardResponse.ItemSummary::from)
                .toList();
    }

    private List<DashboardResponse.ItemSummary> buildInUseItems() {
        return goodItemRepository.findInUseItemsOrderByCreatedAtDesc(
                        PageRequest.of(0, DASHBOARD_LIST_LIMIT)).stream()
                .map(DashboardResponse.ItemSummary::from)
                .toList();
    }

    private List<DashboardResponse.WarrantyExpiringAsset> buildWarrantyExpiringAssets() {
        return assetRepository.findWarrantyExpiringAssets(
                        PageRequest.of(0, DASHBOARD_LIST_LIMIT)).stream()
                .map(DashboardResponse.WarrantyExpiringAsset::from)
                .toList();
    }

    private List<DashboardResponse.InUseAsset> buildInUseAssets() {
        List<Asset> assets = assetRepository.findInUseAssetsOrderByShopDateDesc(
                PageRequest.of(0, DASHBOARD_LIST_LIMIT));
        return assets.stream()
                .map(asset -> DashboardResponse.InUseAsset.from(asset, assetService.computeWarrantyStatus(asset)))
                .toList();
    }

    private List<DashboardResponse.UpcomingRenewal> buildUpcomingRenewals() {
        List<Subscription> subscriptions = subscriptionRepository.findActivePeriodicSubscriptions();
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(UPCOMING_RENEWAL_DAYS);
        List<DashboardResponse.UpcomingRenewal> result = new ArrayList<>();

        for (Subscription sub : subscriptions) {
            SubscriptionRecord latest = recordRepository.findLatestBySubscriptionId(sub.getId()).orElse(null);
            if (latest == null || latest.getEndDate() == null) continue;
            if (latest.getEndDate().isBefore(today) || latest.getEndDate().isAfter(deadline)) continue;

            DashboardResponse.UpcomingRenewal item = new DashboardResponse.UpcomingRenewal();
            item.setId(sub.getId());
            item.setName(sub.getName());
            item.setPlatformName(sub.getPlatform().getName());
            if (sub.getPlatform().getLogoFile() != null) {
                item.setPlatformLogoUrl(OssUrlBuilder.build(
                        sub.getPlatform().getLogoFile().getStoredFilename(),
                        sub.getPlatform().getLogoFile().getOriginalFilename()));
            }
            item.setEndDate(latest.getEndDate());
            result.add(item);
        }

        result.sort(Comparator.comparing(DashboardResponse.UpcomingRenewal::getEndDate));
        if (result.size() > 5) {
            return result.subList(0, 5);
        }
        return result;
    }
}
