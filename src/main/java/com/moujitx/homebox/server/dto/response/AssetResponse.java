package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Asset;
import com.moujitx.homebox.server.entity.AssetPicture;
import com.moujitx.homebox.server.enums.WarrantyStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AssetResponse {

    private Long id;
    private String name;
    private String barcode;
    private String serialNumber;
    private String categoryName;
    private Long categoryId;
    private String placeName;
    private Long placeId;
    private boolean inUse;
    private BigDecimal price;
    private LocalDate shopDate;
    private String storeName;
    private Long storeId;
    private boolean hasWarranty;
    private WarrantyStatus warrantyStatus;
    private LocalDate expirationDate;
    private String note;
    private String firstPictureUrl;
    private int subAssetCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AssetResponse from(Asset asset, WarrantyStatus warrantyStatus, int subAssetCount) {
        AssetResponse response = new AssetResponse();
        populateFromAsset(response, asset, warrantyStatus, subAssetCount);
        return response;
    }

    public static AssetResponse from(Asset asset, WarrantyStatus warrantyStatus,
                                     Map<Long, Integer> subAssetCounts, Map<Long, String> firstPictureUrls) {
        int subAssetCount = subAssetCounts.getOrDefault(asset.getId(), 0);
        AssetResponse response = new AssetResponse();
        populateFromAsset(response, asset, warrantyStatus, subAssetCount);
        response.firstPictureUrl = firstPictureUrls.get(asset.getId());
        return response;
    }

    protected static void populateFromAsset(AssetResponse response, Asset asset, WarrantyStatus warrantyStatus, int subAssetCount) {
        response.id = asset.getId();
        response.name = asset.getName();
        response.barcode = asset.getBarcode();
        response.serialNumber = asset.getSerialNumber();
        response.categoryName = asset.getCategory().getName();
        response.categoryId = asset.getCategory().getId();
        response.placeName = asset.getPlace().getName();
        response.placeId = asset.getPlace().getId();
        response.inUse = asset.isInUse();
        response.price = asset.getPrice();
        response.shopDate = asset.getShopDate();
        response.storeName = asset.getStore() != null ? asset.getStore().getName() : null;
        response.storeId = asset.getStore() != null ? asset.getStore().getId() : null;
        response.hasWarranty = asset.isHasWarranty();
        response.warrantyStatus = warrantyStatus;
        response.expirationDate = asset.getExpirationDate();
        response.note = asset.getNote();
        response.subAssetCount = subAssetCount;

        AssetPicture firstPicture = asset.getPictures().isEmpty() ? null : asset.getPictures().get(0);
        if (firstPicture != null) {
            response.firstPictureUrl = "/api/assets/" + asset.getId() + "/pictures/" + firstPicture.getId() + "/file";
        }

        response.createdAt = asset.getCreatedAt();
        response.updatedAt = asset.getUpdatedAt();
    }
}
