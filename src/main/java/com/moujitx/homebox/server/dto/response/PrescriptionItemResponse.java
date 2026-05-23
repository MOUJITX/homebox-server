package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.PrescriptionItem;

import java.time.LocalDateTime;

public record PrescriptionItemResponse(
    Long id,
    Long prescriptionId,
    Long medicationReminderId,
    String medicationName,
    String dosageMethod,
    String dosageQuantity,
    String dosageUnit,
    String note,
    LocalDateTime createdAt
) {
    public static PrescriptionItemResponse from(PrescriptionItem item) {
        var r = item.getMedicationReminder();
        return new PrescriptionItemResponse(
                item.getId(),
                item.getPrescription().getId(),
                r.getId(),
                r.getGood().getProductName(),
                r.getDosageMethod(),
                r.getDosageQuantity(),
                r.getDosageUnit(),
                item.getNote(),
                item.getCreatedAt()
        );
    }
}
