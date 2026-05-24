package com.moujitx.homebox.server.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreatePrescriptionRequest {

    private LocalDate prescriptionDate;
    private String description;
}
