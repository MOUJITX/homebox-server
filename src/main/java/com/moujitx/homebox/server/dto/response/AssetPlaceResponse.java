package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.AssetPlace;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AssetPlaceResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AssetPlaceResponse from(AssetPlace place) {
        AssetPlaceResponse response = new AssetPlaceResponse();
        response.id = place.getId();
        response.name = place.getName();
        response.description = place.getDescription();
        response.createdAt = place.getCreatedAt();
        response.updatedAt = place.getUpdatedAt();
        return response;
    }
}
