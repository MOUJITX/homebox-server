package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.DocumentInvoice;
import com.moujitx.homebox.server.enums.InvoiceStatus;
import com.moujitx.homebox.server.enums.InvoiceType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class DocumentInvoiceResponse {

    private Long id;
    private Long invoiceId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private InvoiceType invoiceType;
    private InvoiceStatus invoiceStatus;
    private BigDecimal totalAmount;
    private String sellerName;

    public static DocumentInvoiceResponse from(DocumentInvoice binding) {
        DocumentInvoiceResponse response = new DocumentInvoiceResponse();
        response.id = binding.getId();
        response.invoiceId = binding.getInvoice().getId();
        response.invoiceNumber = binding.getInvoice().getInvoiceNumber();
        response.invoiceDate = binding.getInvoice().getInvoiceDate();
        response.invoiceType = binding.getInvoice().getInvoiceType();
        response.invoiceStatus = binding.getInvoice().getInvoiceStatus();
        response.totalAmount = binding.getInvoice().getTotalAmount();
        response.sellerName = binding.getInvoice().getSellerName();
        return response;
    }
}
