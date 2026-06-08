package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.AssetPicture;
import com.moujitx.homebox.server.util.OssUrlBuilder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AssetPictureResponse {

    private Long id;
    private Long fileId;
    private String filename;
    private String contentType;
    private long fileSize;
    private String url;
    private LocalDateTime createdAt;

    public static AssetPictureResponse from(AssetPicture picture) {
        AssetPictureResponse response = new AssetPictureResponse();
        response.id = picture.getId();
        response.fileId = picture.getFile().getId();
        response.filename = picture.getFile().getOriginalFilename();
        response.contentType = picture.getFile().getContentType();
        response.fileSize = picture.getFile().getFileSize();
        response.url = OssUrlBuilder.build(picture.getFile().getStoredFilename(), picture.getFile().getOriginalFilename());
        response.createdAt = picture.getFile().getCreatedAt();
        return response;
    }
}
