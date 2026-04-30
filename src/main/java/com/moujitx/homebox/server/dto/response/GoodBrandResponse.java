package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.GoodBrand;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GoodBrandResponse {

    private Long id;
    private String brandName;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static GoodBrandResponse from(GoodBrand brand) {
        GoodBrandResponse response = new GoodBrandResponse();
        response.id = brand.getId();
        response.brandName = brand.getBrandName();
        response.companyName = brand.getCompanyName();
        response.createdAt = brand.getCreatedAt();
        response.updatedAt = brand.getUpdatedAt();
        return response;
    }
}
