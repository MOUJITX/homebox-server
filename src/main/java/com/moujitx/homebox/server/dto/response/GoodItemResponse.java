package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.GoodItem;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class GoodItemResponse {

    private Long id;
    private LocalDate productDate;
    private LocalDate expirationDate;
    private int lifeDays;
    private boolean inUse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static GoodItemResponse from(GoodItem item) {
        GoodItemResponse response = new GoodItemResponse();
        response.id = item.getId();
        response.productDate = item.getProductDate();
        response.expirationDate = item.getExpirationDate();
        response.lifeDays = item.getLifeDays();
        response.inUse = item.isInUse();
        response.createdAt = item.getCreatedAt();
        response.updatedAt = item.getUpdatedAt();
        return response;
    }
}
