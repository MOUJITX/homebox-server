package com.moujitx.homebox.server.dto.request;

import com.moujitx.homebox.server.enums.InvoiceStatus;
import com.moujitx.homebox.server.enums.InvoiceType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateInvoiceRequest {

    private String invoiceNumber;

    private LocalDate invoiceDate;

    @NotNull
    private InvoiceType invoiceType;

    private InvoiceStatus invoiceStatus;

    private String sellerName;

    private String sellerTaxId;

    private String buyerName;

    private String buyerTaxId;

    private BigDecimal amount;

    private BigDecimal taxAmount;

    @NotNull
    private BigDecimal totalAmount;

    private String remark;

    private Long fileId;
}
