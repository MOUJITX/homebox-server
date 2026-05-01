package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.InvoiceAttachment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InvoiceAttachmentResponse {

    private Long id;
    private String filename;
    private String contentType;
    private long fileSize;
    private String url;
    private LocalDateTime createdAt;

    public static InvoiceAttachmentResponse from(InvoiceAttachment attachment) {
        InvoiceAttachmentResponse response = new InvoiceAttachmentResponse();
        response.id = attachment.getId();
        response.filename = attachment.getFile().getOriginalFilename();
        response.contentType = attachment.getFile().getContentType();
        response.fileSize = attachment.getFile().getFileSize();
        response.url = "/api/invoices/" + attachment.getInvoice().getId() + "/attachments/" + attachment.getId() + "/file";
        response.createdAt = attachment.getFile().getCreatedAt();
        return response;
    }
}
