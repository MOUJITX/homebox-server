package com.moujitx.homebox.server.dto.request;

public class UpdateGoodRequest {

    private String productName;

    private String barcode;

    private Long categoryId;

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
