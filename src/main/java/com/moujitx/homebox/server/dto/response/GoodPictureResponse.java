package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.GoodPicture;

import java.time.LocalDateTime;

public class GoodPictureResponse {

    private Long id;
    private String filename;
    private String contentType;
    private long fileSize;
    private String url;
    private LocalDateTime createdAt;

    public static GoodPictureResponse from(GoodPicture picture) {
        GoodPictureResponse response = new GoodPictureResponse();
        response.id = picture.getId();
        response.filename = picture.getFilename();
        response.contentType = picture.getContentType();
        response.fileSize = picture.getFileSize();
        response.url = "/api/goods/" + picture.getGood().getId() + "/pictures/" + picture.getId() + "/file";
        response.createdAt = picture.getCreatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getUrl() {
        return url;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
