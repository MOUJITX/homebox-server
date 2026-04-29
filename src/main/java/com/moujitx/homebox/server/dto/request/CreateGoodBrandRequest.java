package com.moujitx.homebox.server.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CreateGoodBrandRequest {

    @NotBlank
    private String brandName;

    private String companyName;

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
