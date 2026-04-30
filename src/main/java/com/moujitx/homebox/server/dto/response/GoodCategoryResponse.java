package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.GoodCategory;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GoodCategoryResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static GoodCategoryResponse from(GoodCategory category) {
        GoodCategoryResponse response = new GoodCategoryResponse();
        response.id = category.getId();
        response.name = category.getName();
        response.description = category.getDescription();
        response.createdAt = category.getCreatedAt();
        response.updatedAt = category.getUpdatedAt();
        return response;
    }
}
