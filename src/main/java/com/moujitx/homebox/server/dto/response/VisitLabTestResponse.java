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
        return new VisitLabTestResponse(
                t.getId(),
                t.getVisit().getId(),
                t.getName(),
                t.getTestDate(),
                t.getDescription(),
                0, 0,
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
