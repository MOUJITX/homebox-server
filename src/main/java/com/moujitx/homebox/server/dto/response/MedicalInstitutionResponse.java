package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.MedicalInstitution;

import java.time.LocalDateTime;

public record MedicalInstitutionResponse(
    Long id,
    String name,
    String note,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static MedicalInstitutionResponse from(MedicalInstitution institution) {
        return new MedicalInstitutionResponse(
                institution.getId(),
                institution.getName(),
                institution.getNote(),
                institution.getCreatedAt(),
                institution.getUpdatedAt()
        );
    }
}
