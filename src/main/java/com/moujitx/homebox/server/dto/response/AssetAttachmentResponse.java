package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.AssetAttachment;
import com.moujitx.homebox.server.util.OssUrlBuilder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AssetAttachmentResponse {

    private Long id;
    private String filename;
    private String contentType;
    private long fileSize;
    private String url;
    private LocalDateTime createdAt;
    private boolean indexed;

    public static AssetAttachmentResponse from(AssetAttachment attachment, boolean indexed) {
        AssetAttachmentResponse response = new AssetAttachmentResponse();
        response.id = attachment.getId();
        response.filename = attachment.getFile().getOriginalFilename();
        response.contentType = attachment.getFile().getContentType();
        response.fileSize = attachment.getFile().getFileSize();
        response.url = OssUrlBuilder.build(attachment.getFile().getStoredFilename(), attachment.getFile().getOriginalFilename());
        response.createdAt = attachment.getFile().getCreatedAt();
        response.indexed = indexed;
        return response;
    }
}
