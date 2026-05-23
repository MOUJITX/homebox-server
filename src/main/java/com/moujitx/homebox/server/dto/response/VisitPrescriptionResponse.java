package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.PrescriptionItem;
import com.moujitx.homebox.server.entity.VisitPrescription;

import java.time.LocalDateTime;
import java.util.List;

public record VisitPrescriptionResponse(
    Long id,
    Long visitId,
    String description,
    List<PrescriptionItemResponse> items,
    long attachmentCount,
    long invoiceCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static VisitPrescriptionResponse from(VisitPrescription p) {
        return new VisitPrescriptionResponse(
                p.getId(),
                p.getVisit().getId(),
                p.getDescription(),
                p.getItems().stream().map(PrescriptionItemResponse::from).toList(),
                0, 0,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
