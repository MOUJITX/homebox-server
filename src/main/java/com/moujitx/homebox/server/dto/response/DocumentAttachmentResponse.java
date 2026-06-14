package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.DocumentAttachment;
import com.moujitx.homebox.server.util.OssUrlBuilder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DocumentAttachmentResponse {

    private Long id;
    private Long fileId;
    private String filename;
    private String contentType;
    private long fileSize;
    private String url;
    private LocalDateTime createdAt;
    private boolean indexed;

    public static DocumentAttachmentResponse from(DocumentAttachment attachment, boolean indexed) {
        DocumentAttachmentResponse response = new DocumentAttachmentResponse();
        response.id = attachment.getId();
        response.fileId = attachment.getFile().getId();
        response.filename = attachment.getFile().getOriginalFilename();
        response.contentType = attachment.getFile().getContentType();
        response.fileSize = attachment.getFile().getFileSize();
        response.url = OssUrlBuilder.build(attachment.getFile().getStoredFilename(), attachment.getFile().getOriginalFilename());
        response.createdAt = attachment.getFile().getCreatedAt();
        response.indexed = indexed;
        return response;
    }
}
