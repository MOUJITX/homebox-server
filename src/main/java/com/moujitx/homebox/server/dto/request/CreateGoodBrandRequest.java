package com.moujitx.homebox.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateGoodBrandRequest {

    @NotBlank
    private String brandName;

    private String companyName;
}
