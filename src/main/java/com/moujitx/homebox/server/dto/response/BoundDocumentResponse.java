package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.DocumentInvoice;
import lombok.Getter;

@Getter
public class BoundDocumentResponse {

    private Long id;
    private Long documentId;
    private String documentName;
    private Long categoryId;
    private String categoryName;

    public static BoundDocumentResponse from(DocumentInvoice binding) {
        BoundDocumentResponse response = new BoundDocumentResponse();
        response.id = binding.getId();
        response.documentId = binding.getDocument().getId();
        response.documentName = binding.getDocument().getName();
        response.categoryId = binding.getDocument().getCategory().getId();
        response.categoryName = binding.getDocument().getCategory().getName();
        return response;
    }
}
