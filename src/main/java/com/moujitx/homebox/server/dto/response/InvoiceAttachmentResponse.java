package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.InvoiceAttachment;
import com.moujitx.homebox.server.util.OssUrlBuilder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InvoiceAttachmentResponse {

    private Long id;
    private Long fileId;
    private String filename;
    private String contentType;
    private long fileSize;
    private String url;
    private LocalDateTime createdAt;

    public static InvoiceAttachmentResponse from(InvoiceAttachment attachment) {
        InvoiceAttachmentResponse response = new InvoiceAttachmentResponse();
        response.id = attachment.getId();
        response.fileId = attachment.getFile().getId();
        response.filename = attachment.getFile().getOriginalFilename();
        response.contentType = attachment.getFile().getContentType();
        response.fileSize = attachment.getFile().getFileSize();
        response.url = OssUrlBuilder.build(attachment.getFile().getStoredFilename(), attachment.getFile().getOriginalFilename());
        response.createdAt = attachment.getFile().getCreatedAt();
        return response;
    }
}
