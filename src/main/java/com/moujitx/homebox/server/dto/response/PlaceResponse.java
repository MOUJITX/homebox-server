package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Place;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PlaceResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PlaceResponse from(Place place) {
        PlaceResponse response = new PlaceResponse();
        response.id = place.getId();
        response.name = place.getName();
        response.description = place.getDescription();
        response.createdAt = place.getCreatedAt();
        response.updatedAt = place.getUpdatedAt();
        return response;
    }
}
