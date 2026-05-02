package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.AssetInvoice;
import lombok.Getter;

@Getter
public class BoundAssetResponse {

    private Long id;
    private Long assetId;
    private String name;
    private String barcode;
    private String firstPictureUrl;

    public static BoundAssetResponse from(AssetInvoice binding) {
        BoundAssetResponse response = new BoundAssetResponse();
        response.id = binding.getId();
        response.assetId = binding.getAsset().getId();
        response.name = binding.getAsset().getName();
        response.barcode = binding.getAsset().getBarcode();

        if (!binding.getAsset().getPictures().isEmpty()) {
            Long pictureId = binding.getAsset().getPictures().get(0).getId();
            response.firstPictureUrl = "/api/assets/" + binding.getAsset().getId() + "/pictures/" + pictureId + "/file";
        }

        return response;
    }
}
