package com.moujitx.homebox.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlatformRequest {

    @NotBlank
    private String name;

    private Long logoFileId;

    private String website;
}
