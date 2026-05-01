package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Invoice;
import com.moujitx.homebox.server.enums.InvoiceStatus;
import com.moujitx.homebox.server.enums.InvoiceType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class InvoiceDetailResponse {

    private Long id;
    private String invoiceCode;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private InvoiceType invoiceType;
    private InvoiceStatus invoiceStatus;
    private String sellerName;
    private String sellerTaxId;
    private String buyerName;
    private String buyerTaxId;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String remark;
    private Long fileId;
    private String fileUrl;
    private List<InvoiceAttachmentResponse> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InvoiceDetailResponse from(Invoice invoice) {
        InvoiceDetailResponse response = new InvoiceDetailResponse();
        response.id = invoice.getId();
        response.invoiceCode = invoice.getInvoiceCode();
        response.invoiceNumber = invoice.getInvoiceNumber();
        response.invoiceDate = invoice.getInvoiceDate();
        response.invoiceType = invoice.getInvoiceType();
        response.invoiceStatus = invoice.getInvoiceStatus();
        response.sellerName = invoice.getSellerName();
        response.sellerTaxId = invoice.getSellerTaxId();
        response.buyerName = invoice.getBuyerName();
        response.buyerTaxId = invoice.getBuyerTaxId();
        response.amount = invoice.getAmount();
        response.taxAmount = invoice.getTaxAmount();
        response.totalAmount = invoice.getTotalAmount();
        response.remark = invoice.getRemark();
        response.createdAt = invoice.getCreatedAt();
        response.updatedAt = invoice.getUpdatedAt();

        if (invoice.getFile() != null) {
            response.fileId = invoice.getFile().getId();
            response.fileUrl = "/api/invoices/" + invoice.getId() + "/file/preview";
        }

        response.attachments = invoice.getAttachments().stream()
                .map(InvoiceAttachmentResponse::from)
                .toList();

        return response;
    }
}
