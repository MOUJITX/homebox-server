package com.moujitx.homebox.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateAssetRequest {

    @NotBlank
    private String name;

    private String barcode;

    private String serialNumber;

    @NotNull
    private Long categoryId;

    @NotNull
    private Long placeId;

    private Boolean inUse;

    private BigDecimal price;

    private LocalDate shopDate;

    private Long storeId;

    private Boolean hasWarranty;

    private LocalDate activeDate;

    private Integer warrantyPeriod;

    private LocalDate expirationDate;

    private String note;

    private Long parentId;
}
