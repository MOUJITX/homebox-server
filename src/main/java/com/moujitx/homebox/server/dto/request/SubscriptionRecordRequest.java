package com.moujitx.homebox.server.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class SubscriptionRecordRequest {

    private LocalDate recordDate;

    private BigDecimal amount;

    private String currency;

    private LocalDate startDate;

    private LocalDate endDate;

    private String quantity;

    private String orderNo;

    private Long paymentMethodId;

    private String note;

    private Boolean expired;
}
