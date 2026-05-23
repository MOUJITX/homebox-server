package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.VisitInvoice;
import com.moujitx.homebox.server.enums.VisitSourceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record VisitInvoiceResponse(
    Long id,
    Long visitId,
    Long invoiceId,
    String invoiceNumber,
    LocalDate invoiceDate,
    String invoiceType,
    BigDecimal totalAmount,
    VisitSourceType sourceType,
    Long sourceId,
    LocalDateTime createdAt
) {
    public static VisitInvoiceResponse from(VisitInvoice vi) {
        var inv = vi.getInvoice();
        return new VisitInvoiceResponse(
                vi.getId(),
                vi.getVisit().getId(),
                inv.getId(),
                inv.getInvoiceNumber(),
                inv.getInvoiceDate(),
                inv.getInvoiceType().name(),
                inv.getTotalAmount(),
                vi.getSourceType(),
                vi.getSourceId(),
                vi.getCreatedAt()
        );
    }
}
