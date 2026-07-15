package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.BookInvoice;
import com.moujitx.homebox.server.enums.InvoiceStatus;
import com.moujitx.homebox.server.enums.InvoiceType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class BookInvoiceResponse {

    Long id;
    Long invoiceId;
    String invoiceNumber;
    LocalDate invoiceDate;
    InvoiceType invoiceType;
    InvoiceStatus invoiceStatus;
    BigDecimal totalAmount;
    String sellerName;

    public static BookInvoiceResponse from(BookInvoice binding) {
        BookInvoiceResponse r = new BookInvoiceResponse();
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
