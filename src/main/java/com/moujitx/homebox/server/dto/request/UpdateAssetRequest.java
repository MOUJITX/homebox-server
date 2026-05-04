package com.moujitx.homebox.server.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateAssetRequest {

    private String name;

    private String barcode;

    private String serialNumber;

    private Long categoryId;

    private Long placeId;

    private Boolean inUse;

    private LocalDate retireDate;

    private BigDecimal price;

    private LocalDate shopDate;

    private Long storeId;

    private Boolean hasWarranty;

    private LocalDate activeDate;

    private Integer warrantyPeriod;

    private LocalDate expirationDate;

    private String note;
}
