package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.SubscriptionRecordAttachment;
import com.moujitx.homebox.server.util.OssUrlBuilder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SubscriptionRecordAttachmentResponse {

    private Long id;
    private Long fileId;
    private String filename;
    private String contentType;
    private long fileSize;
    private String url;
    private LocalDateTime createdAt;

    public static SubscriptionRecordAttachmentResponse from(SubscriptionRecordAttachment attachment) {
        SubscriptionRecordAttachmentResponse r = new SubscriptionRecordAttachmentResponse();
        r.id = attachment.getId();
        r.fileId = attachment.getFile().getId();
        r.filename = attachment.getFile().getOriginalFilename();
        r.contentType = attachment.getFile().getContentType();
        r.fileSize = attachment.getFile().getFileSize();
        r.url = OssUrlBuilder.build(attachment.getFile().getStoredFilename(), attachment.getFile().getOriginalFilename());
        r.createdAt = attachment.getFile().getCreatedAt();
        return r;
    }
}
