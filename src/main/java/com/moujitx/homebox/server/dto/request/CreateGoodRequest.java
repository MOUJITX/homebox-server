package com.moujitx.homebox.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public Integer getExpiringSoonDays() {
        return expiringSoonDays;
    }

    public void setExpiringSoonDays(Integer expiringSoonDays) {
        this.expiringSoonDays = expiringSoonDays;
    }
}
