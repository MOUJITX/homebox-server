package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Store;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StoreResponse {

    private Long id;
    private String name;
    private String channel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StoreResponse from(Store store) {
        StoreResponse response = new StoreResponse();
        response.id = store.getId();
        response.name = store.getName();
        response.channel = store.getChannel();
        response.createdAt = store.getCreatedAt();
        response.updatedAt = store.getUpdatedAt();
        return response;
    }
}
