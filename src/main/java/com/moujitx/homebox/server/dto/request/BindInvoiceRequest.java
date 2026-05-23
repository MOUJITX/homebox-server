package com.moujitx.homebox.server.dto.request;

import com.moujitx.homebox.server.enums.VisitSourceType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BindInvoiceRequest {

    @NotNull
    private Long invoiceId;

    @NotNull
    private VisitSourceType sourceType;

    @NotNull
    private Long sourceId;
}
