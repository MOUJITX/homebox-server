package com.moujitx.homebox.server.dto.request;

import com.moujitx.homebox.server.enums.Gender;
import com.moujitx.homebox.server.enums.VisitType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateVisitRecordRequest {

    private String patientName;

    private Integer patientAge;

    private Gender patientGender;

    private VisitType visitType;

    private LocalDate visitDate;

    private Long institutionId;

    private String medicalContent;

    private String doctor;

    private String department;

    private LocalDate dischargeDate;

    private String dischargeDept;
}
