package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.AssetStore;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AssetStoreResponse {

    private Long id;
    private String name;
    private String channel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AssetStoreResponse from(AssetStore store) {
        AssetStoreResponse response = new AssetStoreResponse();
        response.id = store.getId();
        response.name = store.getName();
        response.channel = store.getChannel();
        response.createdAt = store.getCreatedAt();
        response.updatedAt = store.getUpdatedAt();
        return response;
    }
}
