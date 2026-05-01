package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.AssetCategory;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AssetCategoryResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AssetCategoryResponse from(AssetCategory category) {
        AssetCategoryResponse response = new AssetCategoryResponse();
        response.id = category.getId();
        response.name = category.getName();
        response.description = category.getDescription();
        response.createdAt = category.getCreatedAt();
        response.updatedAt = category.getUpdatedAt();
        return response;
    }
}
