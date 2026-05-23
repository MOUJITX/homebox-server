package com.moujitx.homebox.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParseVisitRecordRequest {

    @NotBlank
    private String text;
}
