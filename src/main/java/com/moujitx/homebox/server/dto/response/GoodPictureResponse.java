package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.GoodPicture;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
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
        response.filename = picture.getFile().getOriginalFilename();
        response.contentType = picture.getFile().getContentType();
        response.fileSize = picture.getFile().getFileSize();
        response.url = "/api/goods/" + picture.getGood().getId() + "/pictures/" + picture.getId() + "/file";
        response.createdAt = picture.getFile().getCreatedAt();
        return response;
    }
}
