package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.SubscriptionRecordRequest;
import com.moujitx.homebox.server.dto.request.SyncFileIdsRequest;
import com.moujitx.homebox.server.dto.response.SubscriptionRecordAttachmentResponse;
import com.moujitx.homebox.server.dto.response.SubscriptionRecordInvoiceResponse;
import com.moujitx.homebox.server.dto.response.SubscriptionRecordResponse;
import com.moujitx.homebox.server.service.SubscriptionRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SubscriptionRecordController {

    private final SubscriptionRecordService recordService;

    @GetMapping("/api/subscriptions/{subId}/records")
    public ResponseEntity<List<SubscriptionRecordResponse>> getRecords(@PathVariable Long subId) {
        return ResponseEntity.ok(recordService.getRecords(subId));
    }

    @PostMapping("/api/subscriptions/{subId}/records")
    public ResponseEntity<SubscriptionRecordResponse> addRecord(@PathVariable Long subId,
                                                                  @Valid @RequestBody SubscriptionRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recordService.addRecord(subId, request));
    }

    @PutMapping("/api/subscriptions/{subId}/records/{id}")
    public ResponseEntity<SubscriptionRecordResponse> updateRecord(@PathVariable Long subId,
                                                                     @PathVariable Long id,
                                                                     @Valid @RequestBody SubscriptionRecordRequest request) {
        return ResponseEntity.ok(recordService.updateRecord(subId, id, request));
    }

    @DeleteMapping("/api/subscriptions/{subId}/records/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long subId, @PathVariable Long id) {
        recordService.deleteRecord(subId, id);
        return ResponseEntity.noContent().build();
    }

    // ── Attachments ──

    @PostMapping("/api/subscription-records/{id}/attachments")
    public ResponseEntity<SubscriptionRecordAttachmentResponse> uploadAttachment(@PathVariable Long id,
                                                                                   @RequestParam(value = "file", required = false) MultipartFile file,
                                                                                   @RequestParam(value = "fileId", required = false) Long fileId) {
        if (fileId != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(recordService.linkAttachment(id, fileId));
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(recordService.uploadAttachment(id, file));
    }

    @GetMapping("/api/subscription-records/{id}/attachments")
    public ResponseEntity<List<SubscriptionRecordAttachmentResponse>> getAttachments(@PathVariable Long id) {
        return ResponseEntity.ok(recordService.getAttachments(id));
    }

    @PutMapping("/api/subscription-records/{id}/attachments")
    public ResponseEntity<List<SubscriptionRecordAttachmentResponse>> syncAttachments(@PathVariable Long id,
                                                                                      @RequestBody SyncFileIdsRequest body) {
        return ResponseEntity.ok(recordService.sync(id, body.getFileIds()));
    }

    @DeleteMapping("/api/subscription-records/{id}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id, @PathVariable Long attachmentId) {
        recordService.deleteAttachment(id, attachmentId);
        return ResponseEntity.noContent().build();
    }

    // ── Invoice Bindings ──

    @GetMapping("/api/subscription-records/{id}/invoices")
    public ResponseEntity<List<SubscriptionRecordInvoiceResponse>> getInvoices(@PathVariable Long id) {
        return ResponseEntity.ok(recordService.getInvoices(id));
    }

    @PostMapping("/api/subscription-records/{id}/invoices/{invoiceId}")
    public ResponseEntity<Void> bindInvoice(@PathVariable Long id, @PathVariable Long invoiceId) {
        recordService.bindInvoice(id, invoiceId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/api/subscription-records/{id}/invoices/{invoiceId}")
    public ResponseEntity<Void> unbindInvoice(@PathVariable Long id, @PathVariable Long invoiceId) {
        recordService.unbindInvoice(id, invoiceId);
        return ResponseEntity.noContent().build();
    }
}
