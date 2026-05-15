package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.enums.InvoiceStatus;
import com.moujitx.homebox.server.enums.InvoiceType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
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

    @Setter
    private List<BoundAssetResponse> assets = new ArrayList<>();

    public InvoiceResponse(Long id, String invoiceNumber, LocalDate invoiceDate,
                           InvoiceType invoiceType, InvoiceStatus invoiceStatus,
                           String sellerName, String buyerName,
                           BigDecimal amount, BigDecimal taxAmount, BigDecimal totalAmount,
                           long attachmentCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.invoiceType = invoiceType;
        this.invoiceStatus = invoiceStatus;
        this.sellerName = sellerName;
        this.buyerName = buyerName;
        this.amount = amount;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.attachmentCount = attachmentCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
