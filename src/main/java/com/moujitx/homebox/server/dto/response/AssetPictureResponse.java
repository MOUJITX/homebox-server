package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.AssetPicture;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AssetPictureResponse {

    private Long id;
    private String filename;
    private String contentType;
    private long fileSize;
    private String url;
    private LocalDateTime createdAt;

    public static AssetPictureResponse from(AssetPicture picture) {
        AssetPictureResponse response = new AssetPictureResponse();
        response.id = picture.getId();
        response.filename = picture.getFile().getOriginalFilename();
        response.contentType = picture.getFile().getContentType();
        response.fileSize = picture.getFile().getFileSize();
        response.url = "/api/assets/" + picture.getAsset().getId() + "/pictures/" + picture.getId() + "/file";
        response.createdAt = picture.getFile().getCreatedAt();
        return response;
    }
}
