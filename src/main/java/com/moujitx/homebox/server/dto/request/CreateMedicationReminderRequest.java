package com.moujitx.homebox.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateMedicationReminderRequest {

    @NotNull
    private Long goodId;

    private String dosageMethod;

    private String dosageQuantity;

    private String dosageUnit;

    private String dosageNote;

    @NotBlank
    private String frequencyHours;

    @NotNull
    private LocalDate courseStartDate;

    @NotNull
    private LocalDate courseEndDate;

    private Boolean enabled;
}
