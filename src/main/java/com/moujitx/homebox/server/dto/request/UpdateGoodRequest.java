package com.moujitx.homebox.server.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateGoodRequest {

    private String productName;

    private String barcode;

    private Long categoryId;

    private Long brandId;

    private Integer expiringSoonDays;
}
