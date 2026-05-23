package com.moujitx.homebox.server.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class SubscriptionRecordRequest {

    @NotNull
    private LocalDate recordDate;

    @NotNull
    private BigDecimal amount;

    private String currency;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    private String quantity;

    private String orderNo;

    private Long paymentMethodId;

    private String note;
}
