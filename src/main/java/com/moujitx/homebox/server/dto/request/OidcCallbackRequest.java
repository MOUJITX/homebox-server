package com.moujitx.homebox.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OidcCallbackRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String state;
}
