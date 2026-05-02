package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.enums.InvoiceStatus;
import com.moujitx.homebox.server.enums.InvoiceType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class InvoiceResponse {

    private Long id;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private InvoiceType invoiceType;
    private InvoiceStatus invoiceStatus;
    private String sellerName;
    private String buyerName;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private long attachmentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
