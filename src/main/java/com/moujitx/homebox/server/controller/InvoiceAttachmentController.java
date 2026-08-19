package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.SyncFileIdsRequest;
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

import java.util.List;

@RestController
@RequestMapping("/api/invoices/{invoiceId}/attachments")
@RequiredArgsConstructor
public class InvoiceAttachmentController {

    private final InvoiceService invoiceService;
    private final FileService fileService;

    @PutMapping
    public ResponseEntity<List<InvoiceAttachmentResponse>> syncAttachments(@PathVariable Long invoiceId,
                                                                           @RequestBody SyncFileIdsRequest body) {
        return ResponseEntity.ok(invoiceService.sync(invoiceId, body.getFileIds()));
    }

    @PostMapping
    public ResponseEntity<InvoiceAttachmentResponse> uploadAttachment(@PathVariable Long invoiceId,
                                                                       @RequestParam(value = "file", required = false) MultipartFile file,
                                                                       @RequestParam(value = "fileId", required = false) Long fileId) {
        if (fileId != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.linkAttachment(invoiceId, fileId));
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
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
