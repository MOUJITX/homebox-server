package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Asset;
import com.moujitx.homebox.server.entity.GoodItem;
import com.moujitx.homebox.server.enums.WarrantyStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private Stats stats;
    private List<ItemSummary> expiringSoonItems;
    private List<ItemSummary> inUseItems;
    private List<WarrantyExpiringAsset> warrantyExpiringAssets;
    private List<InUseAsset> inUseAssets;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private long itemCount;
        private long assetCount;
        private BigDecimal totalAssetPrice;
        private long invoiceCount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemSummary {
        private Long id;
        private Long goodId;
        private String productName;
        private String categoryName;
        private String brandName;
        private LocalDate expirationDate;
        private int lifeDays;
        private LocalDateTime createdAt;

        public static ItemSummary from(GoodItem item) {
            ItemSummary dto = new ItemSummary();
            dto.id = item.getId();
            dto.goodId = item.getGood().getId();
            dto.productName = item.getGood().getProductName();
            dto.categoryName = item.getGood().getCategory().getName();
            dto.brandName = item.getGood().getBrand().getBrandName();
            dto.expirationDate = item.getExpirationDate();
            dto.lifeDays = item.getLifeDays();
            dto.createdAt = item.getCreatedAt();
            return dto;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarrantyExpiringAsset {
        private Long id;
        private String name;
        private String categoryName;
        private String placeName;
        private BigDecimal price;
        private LocalDate expirationDate;
        private LocalDate shopDate;

        public static WarrantyExpiringAsset from(Asset asset) {
            WarrantyExpiringAsset dto = new WarrantyExpiringAsset();
            dto.id = asset.getId();
            dto.name = asset.getName();
            dto.categoryName = asset.getCategory().getName();
            dto.placeName = asset.getPlace().getName();
            dto.price = asset.getPrice();
            dto.expirationDate = asset.getExpirationDate();
            dto.shopDate = asset.getShopDate();
            return dto;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InUseAsset {
        private Long id;
        private String name;
        private String categoryName;
        private String placeName;
        private BigDecimal price;
        private LocalDate shopDate;
        private boolean hasWarranty;
        private WarrantyStatus warrantyStatus;
        private LocalDate expirationDate;

        public static InUseAsset from(Asset asset, WarrantyStatus warrantyStatus) {
            InUseAsset dto = new InUseAsset();
            dto.id = asset.getId();
            dto.name = asset.getName();
            dto.categoryName = asset.getCategory().getName();
            dto.placeName = asset.getPlace().getName();
            dto.price = asset.getPrice();
            dto.shopDate = asset.getShopDate();
            dto.hasWarranty = asset.isHasWarranty();
            dto.warrantyStatus = warrantyStatus;
            dto.expirationDate = asset.getExpirationDate();
            return dto;
        }
    }
}
