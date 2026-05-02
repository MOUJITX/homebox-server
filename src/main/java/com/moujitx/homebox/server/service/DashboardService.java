package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.response.DashboardResponse;
import com.moujitx.homebox.server.entity.Asset;
import com.moujitx.homebox.server.entity.GoodItem;
import com.moujitx.homebox.server.enums.ItemStatus;
import com.moujitx.homebox.server.repository.AssetRepository;
import com.moujitx.homebox.server.repository.GoodItemRepository;
import com.moujitx.homebox.server.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final GoodItemRepository goodItemRepository;
    private final AssetRepository assetRepository;
    private final InvoiceRepository invoiceRepository;
    private final AssetService assetService;

    private static final int DASHBOARD_LIST_LIMIT = 10;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        DashboardResponse.Stats stats = buildStats();
        List<DashboardResponse.ExpiringSoonItem> expiringSoonItems = buildExpiringSoonItems();
        List<DashboardResponse.InUseItem> inUseItems = buildInUseItems();
        List<DashboardResponse.WarrantyExpiringAsset> warrantyExpiringAssets = buildWarrantyExpiringAssets();
        List<DashboardResponse.InUseAsset> inUseAssets = buildInUseAssets();

        return new DashboardResponse(stats, expiringSoonItems, inUseItems, warrantyExpiringAssets, inUseAssets);
    }

    private DashboardResponse.Stats buildStats() {
        long itemCount = goodItemRepository.countInUseItems();
        long assetCount = assetRepository.count();
        var totalAssetPrice = assetRepository.sumAllPrices();
        long invoiceCount = invoiceRepository.count();
        return new DashboardResponse.Stats(itemCount, assetCount, totalAssetPrice, invoiceCount);
    }

    private List<DashboardResponse.ExpiringSoonItem> buildExpiringSoonItems() {
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
                .map(DashboardResponse.ExpiringSoonItem::from)
                .toList();
    }

    private List<DashboardResponse.InUseItem> buildInUseItems() {
        return goodItemRepository.findInUseItemsOrderByCreatedAtDesc(
                        PageRequest.of(0, DASHBOARD_LIST_LIMIT)).stream()
                .map(DashboardResponse.InUseItem::from)
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
}
