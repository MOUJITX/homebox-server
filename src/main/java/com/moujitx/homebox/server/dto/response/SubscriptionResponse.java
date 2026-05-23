package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Subscription;
import com.moujitx.homebox.server.enums.BillingMode;
import com.moujitx.homebox.server.enums.SubscriptionStatus;
import com.moujitx.homebox.server.enums.SubscriptionType;
import com.moujitx.homebox.server.util.OssUrlBuilder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class SubscriptionResponse {

    private Long id;
    private String name;
    private String description;
    private SubscriptionType subscriptionType;
    private BillingMode billingMode;
    private Long platformId;
    private String platformName;
    private String platformLogoUrl;
    private SubscriptionStatus status;
    private Integer renewNoticeDays;
    private String note;
    @Setter
    private LocalDate latestRecordDate;
    @Setter
    private BigDecimal latestRecordAmount;
    @Setter
    private LocalDate latestRecordEndDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Setter
    private boolean hasInvoice;

    public static SubscriptionResponse from(Subscription s) {
        SubscriptionResponse r = new SubscriptionResponse();
        r.id = s.getId();
        r.name = s.getName();
        r.description = s.getDescription();
        r.subscriptionType = s.getSubscriptionType();
        r.billingMode = s.getBillingMode();
        r.platformId = s.getPlatform().getId();
        r.platformName = s.getPlatform().getName();
        if (s.getPlatform().getLogoFile() != null) {
            r.platformLogoUrl = OssUrlBuilder.build(
                    s.getPlatform().getLogoFile().getStoredFilename(),
                    s.getPlatform().getLogoFile().getOriginalFilename());
        }
        r.status = s.getStatus();
        r.renewNoticeDays = s.getRenewNoticeDays();
        r.note = s.getNote();
        r.createdAt = s.getCreatedAt();
        r.updatedAt = s.getUpdatedAt();
        return r;
    }
}
