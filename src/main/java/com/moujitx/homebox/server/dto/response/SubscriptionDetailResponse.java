package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Subscription;
import com.moujitx.homebox.server.entity.SubscriptionRecord;
import com.moujitx.homebox.server.enums.BillingMode;
import com.moujitx.homebox.server.enums.SubscriptionStatus;
import com.moujitx.homebox.server.enums.SubscriptionType;
import com.moujitx.homebox.server.util.OssUrlBuilder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class SubscriptionDetailResponse {

    private Long id;
    private String name;
    private String description;
    private SubscriptionType subscriptionType;
    private BillingMode billingMode;
    private Integer billingCycleDays;
    private BigDecimal price;
    private String currency;
    private Long platformId;
    private String platformName;
    private String platformLogoUrl;
    private String platformWebsite;
    private SubscriptionStatus status;
    private Integer renewNoticeDays;
    private String note;
    private List<SubscriptionRecordResponse> records;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SubscriptionDetailResponse from(Subscription s, List<SubscriptionRecordResponse> records) {
        SubscriptionDetailResponse r = new SubscriptionDetailResponse();
        r.id = s.getId();
        r.name = s.getName();
        r.description = s.getDescription();
        r.subscriptionType = s.getSubscriptionType();
        r.billingMode = s.getBillingMode();
        r.billingCycleDays = s.getBillingCycleDays();
        r.price = s.getPrice();
        r.currency = s.getCurrency();
        r.platformId = s.getPlatform().getId();
        r.platformName = s.getPlatform().getName();
        r.platformWebsite = s.getPlatform().getWebsite();
        if (s.getPlatform().getLogoFile() != null) {
            r.platformLogoUrl = OssUrlBuilder.build(
                    s.getPlatform().getLogoFile().getStoredFilename(),
                    s.getPlatform().getLogoFile().getOriginalFilename());
        }
        r.status = s.getStatus();
        r.renewNoticeDays = s.getRenewNoticeDays();
        r.note = s.getNote();
        r.records = records;
        r.createdAt = s.getCreatedAt();
        r.updatedAt = s.getUpdatedAt();
        return r;
    }
}
