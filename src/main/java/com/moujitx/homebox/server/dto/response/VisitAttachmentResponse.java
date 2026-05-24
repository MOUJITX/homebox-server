package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.VisitAttachment;
import com.moujitx.homebox.server.enums.VisitSourceType;
import com.moujitx.homebox.server.util.OssUrlBuilder;

import java.time.LocalDateTime;

public record VisitAttachmentResponse(
    Long id,
    Long visitId,
    Long fileId,
    String originalFilename,
    Long fileSize,
    String url,
    VisitSourceType sourceType,
    Long sourceId,
    LocalDateTime createdAt
) {
    public static VisitAttachmentResponse from(VisitAttachment a) {
        return new VisitAttachmentResponse(
                a.getId(),
                a.getVisit().getId(),
                a.getFile().getId(),
                a.getFile().getOriginalFilename(),
                a.getFile().getFileSize(),
                OssUrlBuilder.build(a.getFile().getStoredFilename(), a.getFile().getOriginalFilename()),
                a.getSourceType(),
                a.getSourceId(),
                a.getCreatedAt()
        );
    }
}
