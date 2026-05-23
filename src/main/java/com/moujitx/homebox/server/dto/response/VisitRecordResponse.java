package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.VisitRecord;
import com.moujitx.homebox.server.enums.Gender;
import com.moujitx.homebox.server.enums.VisitType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record VisitRecordResponse(
    Long id,
    String patientName,
    Integer patientAge,
    Gender patientGender,
    VisitType visitType,
    LocalDate visitDate,
    Long institutionId,
    String institutionName,
    String medicalContent,
    String doctor,
    String department,
    LocalDate dischargeDate,
    String dischargeDept,
    Long hospitalizationDays,
    long examinationCount,
    long labTestCount,
    long prescriptionCount,
    long attachmentCount,
    long invoiceCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static VisitRecordResponse from(VisitRecord v) {
        Long hospDays = null;
        if (v.getDischargeDate() != null && v.getVisitDate() != null) {
            hospDays = ChronoUnit.DAYS.between(v.getVisitDate(), v.getDischargeDate());
        }
        return new VisitRecordResponse(
                v.getId(),
                v.getPatientName(),
                v.getPatientAge(),
                v.getPatientGender(),
                v.getVisitType(),
                v.getVisitDate(),
                v.getInstitution().getId(),
                v.getInstitution().getName(),
                v.getMedicalContent(),
                v.getDoctor(),
                v.getDepartment(),
                v.getDischargeDate(),
                v.getDischargeDept(),
                hospDays,
                v.getExaminations().size(),
                v.getLabTests().size(),
                v.getPrescriptions().size(),
                v.getAttachments().size(),
                v.getInvoices().size(),
                v.getCreatedAt(),
                v.getUpdatedAt()
        );
    }
}
