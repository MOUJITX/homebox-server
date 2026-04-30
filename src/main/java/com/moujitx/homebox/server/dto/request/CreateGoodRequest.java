package com.moujitx.homebox.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateGoodRequest {

    @NotBlank
    private String productName;

    @NotBlank
    private String barcode;

    @NotNull
    private Long categoryId;

    @NotNull
    private Long brandId;

    private Integer expiringSoonDays;
}
