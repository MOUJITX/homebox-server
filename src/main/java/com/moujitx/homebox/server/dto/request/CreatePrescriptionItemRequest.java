package com.moujitx.homebox.server.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePrescriptionItemRequest {

    @NotNull
    private Long medicationReminderId;

    private String note;
}
