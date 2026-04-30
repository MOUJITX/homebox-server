package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Good;
import com.moujitx.homebox.server.entity.GoodPicture;
import com.moujitx.homebox.server.enums.GoodStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GoodResponse {

    private Long id;
    private String productName;
    private String barcode;
    private String categoryName;
    private Long categoryId;
    private String brandName;
    private Long brandId;
    private int expiringSoonDays;
    private int itemCountTotal;
    private int itemCountInUse;
    private GoodStatus status;
    private String firstPictureUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static GoodResponse from(Good good, GoodStatus status) {
        GoodResponse response = new GoodResponse();
        populateFromGood(response, good, status);
        return response;
    }

    protected static void populateFromGood(GoodResponse response, Good good, GoodStatus status) {
        response.id = good.getId();
        response.productName = good.getProductName();
        response.barcode = good.getBarcode();
        response.categoryName = good.getCategory().getName();
        response.categoryId = good.getCategory().getId();
        response.brandName = good.getBrand().getBrandName();
        response.brandId = good.getBrand().getId();
        response.expiringSoonDays = good.getExpiringSoonDays();
        response.itemCountTotal = good.getItems().size();
        response.itemCountInUse = (int) good.getItems().stream().filter(i -> i.isInUse()).count();
        response.status = status;

        GoodPicture firstPicture = good.getPictures().isEmpty() ? null : good.getPictures().get(0);
        if (firstPicture != null) {
            response.firstPictureUrl = "/api/goods/" + good.getId() + "/pictures/" + firstPicture.getId() + "/file";
        }

        response.createdAt = good.getCreatedAt();
        response.updatedAt = good.getUpdatedAt();
    }
}
