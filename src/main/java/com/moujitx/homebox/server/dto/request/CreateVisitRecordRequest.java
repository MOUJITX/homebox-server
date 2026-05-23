package com.moujitx.homebox.server.dto.request;

import com.moujitx.homebox.server.enums.Gender;
import com.moujitx.homebox.server.enums.VisitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateVisitRecordRequest {

    @NotBlank
    private String patientName;

    private Integer patientAge;

    private Gender patientGender;

    @NotNull
    private VisitType visitType;

    @NotNull
    private LocalDate visitDate;

    @NotNull
    private Long institutionId;

    private String medicalContent;

    private String doctor;

    private String department;

    private LocalDate dischargeDate;

    private String dischargeDept;
}
