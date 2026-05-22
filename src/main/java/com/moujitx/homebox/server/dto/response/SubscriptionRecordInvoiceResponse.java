package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.SubscriptionRecordInvoice;
import com.moujitx.homebox.server.enums.InvoiceStatus;
import com.moujitx.homebox.server.enums.InvoiceType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class SubscriptionRecordInvoiceResponse {

    private Long id;
    private Long invoiceId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private InvoiceType invoiceType;
    private InvoiceStatus invoiceStatus;
    private BigDecimal totalAmount;
    private String sellerName;

    public static SubscriptionRecordInvoiceResponse from(SubscriptionRecordInvoice binding) {
        SubscriptionRecordInvoiceResponse r = new SubscriptionRecordInvoiceResponse();
        r.id = binding.getId();
        r.invoiceId = binding.getInvoice().getId();
        r.invoiceNumber = binding.getInvoice().getInvoiceNumber();
        r.invoiceDate = binding.getInvoice().getInvoiceDate();
        r.invoiceType = binding.getInvoice().getInvoiceType();
        r.invoiceStatus = binding.getInvoice().getInvoiceStatus();
        r.totalAmount = binding.getInvoice().getTotalAmount();
        r.sellerName = binding.getInvoice().getSellerName();
        return r;
    }
}
