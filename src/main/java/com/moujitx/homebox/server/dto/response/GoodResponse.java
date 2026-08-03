package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Good;
import com.moujitx.homebox.server.entity.GoodPicture;
import com.moujitx.homebox.server.enums.GoodStatus;
import com.moujitx.homebox.server.util.OssUrlBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
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
    private List<GoodItemBriefResponse> briefItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static GoodResponse from(Good good, GoodStatus status) {
        GoodResponse response = new GoodResponse();
        populateFromGood(response, good, status);
        return response;
    }

    public static GoodResponse from(Good good, GoodStatus status,
                                    Map<Long, Integer> itemCountTotals, Map<Long, Integer> itemCountInUses,
                                    Map<Long, String> firstPictureUrls) {
        GoodResponse response = new GoodResponse();
        populateFromGood(response, good, status);
        response.itemCountTotal = itemCountTotals.getOrDefault(good.getId(), 0);
        response.itemCountInUse = itemCountInUses.getOrDefault(good.getId(), 0);
        response.firstPictureUrl = firstPictureUrls.get(good.getId());
        return response;
    }

    public static GoodResponse from(Good good, GoodStatus status,
                                    Map<Long, Integer> itemCountTotals, Map<Long, Integer> itemCountInUses,
                                    Map<Long, String> firstPictureUrls,
                                    List<GoodItemBriefResponse> briefItems) {
        GoodResponse response = from(good, status, itemCountTotals, itemCountInUses, firstPictureUrls);
        response.briefItems = briefItems;
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
            response.firstPictureUrl = OssUrlBuilder.build(firstPicture.getFile().getStoredFilename(), firstPicture.getFile().getOriginalFilename());
        }

        response.createdAt = good.getCreatedAt();
        response.updatedAt = good.getUpdatedAt();
    }
}

