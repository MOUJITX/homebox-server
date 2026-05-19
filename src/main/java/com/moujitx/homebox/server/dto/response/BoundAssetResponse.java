package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.AssetInvoice;
import com.moujitx.homebox.server.util.OssUrlBuilder;
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
            response.firstPictureUrl = OssUrlBuilder.build(binding.getAsset().getPictures().get(0).getFile().getStoredFilename(), binding.getAsset().getPictures().get(0).getFile().getOriginalFilename());
        }

        return response;
    }
}
