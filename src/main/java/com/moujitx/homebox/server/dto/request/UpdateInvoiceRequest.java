package com.moujitx.homebox.server.dto.request;

import com.moujitx.homebox.server.enums.InvoiceStatus;
import com.moujitx.homebox.server.enums.InvoiceType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateInvoiceRequest {

    private String invoiceCode;

    private String invoiceNumber;

    private LocalDate invoiceDate;

    private InvoiceType invoiceType;

    private InvoiceStatus invoiceStatus;

    private String checkCode;

    private String machineNumber;

    private String sellerName;

    private String sellerTaxId;

    private String buyerName;

    private String buyerTaxId;

    private BigDecimal amount;

    private BigDecimal taxAmount;

    private BigDecimal totalAmount;

    private String payee;

    private String reviewer;

    private String issuer;

    private String remark;
}
