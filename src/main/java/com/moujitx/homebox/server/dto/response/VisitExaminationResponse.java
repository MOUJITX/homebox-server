package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.VisitExamination;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record VisitExaminationResponse(
    Long id,
    Long visitId,
    String name,
    LocalDate examDate,
    String description,
    long attachmentCount,
    long invoiceCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static VisitExaminationResponse from(VisitExamination e) {
        return from(e, 0, 0);
    }

    public static VisitExaminationResponse from(VisitExamination e, long attachmentCount, long invoiceCount) {
        return new VisitExaminationResponse(
                e.getId(),
                e.getVisit().getId(),
                e.getName(),
                e.getExamDate(),
                e.getDescription(),
                attachmentCount,
                invoiceCount,
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
