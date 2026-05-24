package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.enums.Gender;
import com.moujitx.homebox.server.enums.VisitType;

import java.time.LocalDate;

public record VisitRecordParseResponse(
    String patientName,
    Integer patientAge,
    Gender patientGender,
    VisitType visitType,
    LocalDate visitDate,
    String medicalContent,
    String diagnosis,
    String doctor,
    String department,
    LocalDate dischargeDate,
    String dischargeDept
) {}
