package com.moujitx.homebox.server.dto.request;

import com.moujitx.homebox.server.enums.BillingMode;
import com.moujitx.homebox.server.enums.SubscriptionStatus;
import com.moujitx.homebox.server.enums.SubscriptionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SubscriptionRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private SubscriptionType subscriptionType;

    private BillingMode billingMode;

    private Integer billingCycleDays;

    private BigDecimal price;

    private String currency;

    @NotNull
    private Long platformId;

    private SubscriptionStatus status;

    private Integer renewNoticeDays;

    private String note;
}
