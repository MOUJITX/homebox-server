package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Invoice;
import com.moujitx.homebox.server.enums.InvoiceStatus;
import com.moujitx.homebox.server.enums.InvoiceType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class InvoiceResponse {

    private Long id;
    private String invoiceCode;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private InvoiceType invoiceType;
    private InvoiceStatus invoiceStatus;
    private String sellerName;
    private String buyerName;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private int attachmentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InvoiceResponse from(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.id = invoice.getId();
        response.invoiceCode = invoice.getInvoiceCode();
        response.invoiceNumber = invoice.getInvoiceNumber();
        response.invoiceDate = invoice.getInvoiceDate();
        response.invoiceType = invoice.getInvoiceType();
        response.invoiceStatus = invoice.getInvoiceStatus();
        response.sellerName = invoice.getSellerName();
        response.buyerName = invoice.getBuyerName();
        response.amount = invoice.getAmount();
        response.taxAmount = invoice.getTaxAmount();
        response.totalAmount = invoice.getTotalAmount();
        response.attachmentCount = invoice.getAttachments().size();
        response.createdAt = invoice.getCreatedAt();
        response.updatedAt = invoice.getUpdatedAt();
        return response;
    }
}
