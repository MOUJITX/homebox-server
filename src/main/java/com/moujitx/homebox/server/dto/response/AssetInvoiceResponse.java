package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.AssetInvoice;
import com.moujitx.homebox.server.enums.InvoiceStatus;
import com.moujitx.homebox.server.enums.InvoiceType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class AssetInvoiceResponse {

    private Long id;
    private Long invoiceId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private InvoiceType invoiceType;
    private InvoiceStatus invoiceStatus;
    private BigDecimal totalAmount;

    public static AssetInvoiceResponse from(AssetInvoice binding) {
        AssetInvoiceResponse response = new AssetInvoiceResponse();
        response.id = binding.getId();
        response.invoiceId = binding.getInvoice().getId();
        response.invoiceNumber = binding.getInvoice().getInvoiceNumber();
        response.invoiceDate = binding.getInvoice().getInvoiceDate();
        response.invoiceType = binding.getInvoice().getInvoiceType();
        response.invoiceStatus = binding.getInvoice().getInvoiceStatus();
        response.totalAmount = binding.getInvoice().getTotalAmount();
        return response;
    }
}
