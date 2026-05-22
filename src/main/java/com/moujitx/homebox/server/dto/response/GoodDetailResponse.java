package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Good;
import com.moujitx.homebox.server.entity.GoodItem;
import com.moujitx.homebox.server.enums.GoodStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class GoodDetailResponse extends GoodResponse {

    private List<GoodItemResponse> items;
    private List<GoodPictureResponse> pictures;
    @Setter
    private List<GoodAttachmentResponse> attachments;

    public static GoodDetailResponse from(Good good, GoodStatus status) {
        GoodDetailResponse response = new GoodDetailResponse();
        populateFromGood(response, good, status);

        response.items = good.getItems().stream()
                .map(GoodItemResponse::from)
                .toList();
        response.pictures = good.getPictures().stream()
                .map(GoodPictureResponse::from)
                .toList();

        return response;
    }

    public static GoodDetailResponse from(Good good, GoodStatus status, List<GoodItem> sortedItems) {
        GoodDetailResponse response = new GoodDetailResponse();
        populateFromGood(response, good, status);

        response.items = sortedItems.stream()
                .map(GoodItemResponse::from)
                .toList();
        response.pictures = good.getPictures().stream()
                .map(GoodPictureResponse::from)
                .toList();

        return response;
    }
}
