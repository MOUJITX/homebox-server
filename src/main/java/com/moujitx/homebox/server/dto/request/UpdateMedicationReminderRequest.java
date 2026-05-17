package com.moujitx.homebox.server.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateMedicationReminderRequest {

    private Long goodId;

    private String dosageMethod;

    private String dosageQuantity;

    private String dosageUnit;

    private String dosageNote;

    private String frequencyHours;

    private LocalDate courseStartDate;

    private LocalDate courseEndDate;

    private Boolean enabled;
}
