package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Document;
import com.moujitx.homebox.server.enums.DocumentStatus;
import com.moujitx.homebox.server.enums.Importance;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class DocumentDetailResponse extends DocumentResponse {

    private List<DocumentResponse> subDocuments;
    private List<DocumentInvoiceResponse> invoices;
    @Setter
    private List<DocumentAttachmentResponse> attachments;

    public static DocumentDetailResponse from(Document document, int subDocumentCount,
                                               List<DocumentResponse> subDocuments,
                                               List<DocumentInvoiceResponse> invoices) {
        DocumentDetailResponse response = new DocumentDetailResponse();
        populateFromDocument(response, document, subDocumentCount);
        response.subDocuments = subDocuments;
        response.invoices = invoices;
        return response;
    }
}
