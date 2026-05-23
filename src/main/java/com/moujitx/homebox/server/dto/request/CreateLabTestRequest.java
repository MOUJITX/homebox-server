package com.moujitx.homebox.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateLabTestRequest {

    @NotBlank
    private String name;

    private LocalDate testDate;

    private String description;
}
