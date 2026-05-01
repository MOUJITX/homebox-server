package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.response.InvoiceAttachmentResponse;
import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.service.FileService;
import com.moujitx.homebox.server.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/invoices/{invoiceId}/attachments")
@RequiredArgsConstructor
public class InvoiceAttachmentController {

    private final InvoiceService invoiceService;
    private final FileService fileService;

    @PostMapping
    public ResponseEntity<InvoiceAttachmentResponse> uploadAttachment(@PathVariable Long invoiceId,
                                                                       @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.uploadAttachment(invoiceId, file));
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long invoiceId, @PathVariable Long attachmentId) {
        invoiceService.deleteAttachment(invoiceId, attachmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{attachmentId}/file")
    public ResponseEntity<byte[]> getFile(@PathVariable Long invoiceId, @PathVariable Long attachmentId) {
        FileRecord file = invoiceService.getAttachmentFile(invoiceId, attachmentId);
        byte[] data = fileService.loadFileContent(file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));
        headers.setContentLength(data.length);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
