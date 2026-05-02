package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Asset;
import com.moujitx.homebox.server.enums.WarrantyStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class AssetDetailResponse extends AssetResponse {

    private LocalDate activeDate;
    private Integer warrantyPeriod;
    private List<AssetPictureResponse> pictures;
    private List<AssetResponse> subAssets;
    private List<AssetInvoiceResponse> invoices;

    public static AssetDetailResponse from(Asset asset, WarrantyStatus warrantyStatus, int subAssetCount,
                                            List<AssetResponse> subAssets) {
        return from(asset, warrantyStatus, subAssetCount, subAssets, List.of());
    }

    public static AssetDetailResponse from(Asset asset, WarrantyStatus warrantyStatus, int subAssetCount,
                                            List<AssetResponse> subAssets, List<AssetInvoiceResponse> invoices) {
        AssetDetailResponse response = new AssetDetailResponse();
        populateFromAsset(response, asset, warrantyStatus, subAssetCount);

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
