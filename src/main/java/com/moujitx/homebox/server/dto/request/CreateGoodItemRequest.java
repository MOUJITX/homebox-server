package com.moujitx.homebox.server.dto.request;

import java.time.LocalDate;

public class CreateGoodItemRequest {

    private LocalDate productDate;

    private LocalDate expirationDate;

    private Integer lifeDays;

    private Boolean inUse;

    public LocalDate getProductDate() {
        return productDate;
    }

    public void setProductDate(LocalDate productDate) {
        this.productDate = productDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Integer getLifeDays() {
        return lifeDays;
    }

    public void setLifeDays(Integer lifeDays) {
        this.lifeDays = lifeDays;
    }

    public Boolean getInUse() {
        return inUse;
    }

    public void setInUse(Boolean inUse) {
        this.inUse = inUse;
    }
}
