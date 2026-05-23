package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.SubscriptionRecord;
import com.moujitx.homebox.server.util.OssUrlBuilder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class SubscriptionRecordResponse {

    private Long id;
    private Long subscriptionId;
    private LocalDate recordDate;
    private BigDecimal amount;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
    private String quantity;
    private String orderNo;
    private Long paymentMethodId;
    private String paymentMethodName;
    private String paymentMethodLogoUrl;
    private String note;
    private boolean expired;
    private List<SubscriptionRecordAttachmentResponse> attachments;
    private List<SubscriptionRecordInvoiceResponse> invoices;
    private LocalDateTime createdAt;

    public static SubscriptionRecordResponse from(SubscriptionRecord record) {
        SubscriptionRecordResponse r = new SubscriptionRecordResponse();
        r.id = record.getId();
        r.subscriptionId = record.getSubscription().getId();
        r.recordDate = record.getRecordDate();
        r.amount = record.getAmount();
        r.currency = record.getCurrency();
        r.startDate = record.getStartDate();
        r.endDate = record.getEndDate();
        r.quantity = record.getQuantity();
        r.orderNo = record.getOrderNo();
        if (record.getPaymentMethod() != null) {
            r.paymentMethodId = record.getPaymentMethod().getId();
            r.paymentMethodName = record.getPaymentMethod().getName();
            if (record.getPaymentMethod().getLogoFile() != null) {
                r.paymentMethodLogoUrl = OssUrlBuilder.build(
                        record.getPaymentMethod().getLogoFile().getStoredFilename(),
                        record.getPaymentMethod().getLogoFile().getOriginalFilename());
            }
        }
        r.note = record.getNote();
        r.expired = record.isExpired();
        r.attachments = record.getAttachments().stream()
                .map(SubscriptionRecordAttachmentResponse::from)
                .toList();
        r.invoices = record.getInvoiceBindings().stream()
                .map(SubscriptionRecordInvoiceResponse::from)
                .toList();
        r.createdAt = record.getCreatedAt();
        return r;
    }
}
