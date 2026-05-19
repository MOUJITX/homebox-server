package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Asset;
import com.moujitx.homebox.server.enums.WarrantyStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
public class AssetDetailResponse extends AssetResponse {

    private LocalDate activeDate;
    private Integer warrantyPeriod;
    private List<AssetPictureResponse> pictures;
    private List<AssetResponse> subAssets;
    private List<AssetInvoiceResponse> invoices;
    @Setter
    private List<AssetAttachmentResponse> attachments;

    public static AssetDetailResponse from(Asset asset, WarrantyStatus warrantyStatus, int subAssetCount,
                                            List<AssetResponse> subAssets) {
        return from(asset, warrantyStatus, subAssetCount, subAssets, List.of());
    }

    public static AssetDetailResponse from(Asset asset, WarrantyStatus warrantyStatus, int subAssetCount,
                                            List<AssetResponse> subAssets, List<AssetInvoiceResponse> invoices) {
        AssetDetailResponse response = new AssetDetailResponse();
        populateFromAsset(response, asset, warrantyStatus, subAssetCount);

        BigDecimal ownPrice = asset.getPrice() != null ? asset.getPrice() : BigDecimal.ZERO;
        BigDecimal childSum = subAssets.stream()
                .map(AssetResponse::getPrice)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        setTotalPrice(response, ownPrice.add(childSum));

        response.activeDate = asset.getActiveDate();
        response.warrantyPeriod = asset.getWarrantyPeriod();
        response.pictures = asset.getPictures().stream()
                .map(AssetPictureResponse::from)
                .toList();
        response.subAssets = subAssets;
        response.invoices = invoices;

        return response;
    }
}
