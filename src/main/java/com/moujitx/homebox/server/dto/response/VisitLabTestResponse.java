package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.VisitLabTest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record VisitLabTestResponse(
    Long id,
    Long visitId,
    String name,
    LocalDate testDate,
    String description,
    long attachmentCount,
    long invoiceCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static VisitLabTestResponse from(VisitLabTest t) {
        return from(t, 0, 0);
    }

    public static VisitLabTestResponse from(VisitLabTest t, long attachmentCount, long invoiceCount) {
        return new VisitLabTestResponse(
                t.getId(),
                t.getVisit().getId(),
                t.getName(),
                t.getTestDate(),
                t.getDescription(),
                attachmentCount,
                invoiceCount,
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
