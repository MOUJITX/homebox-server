package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateDocumentRequest;
import com.moujitx.homebox.server.dto.request.UpdateDocumentRequest;
import com.moujitx.homebox.server.dto.response.DocumentDetailResponse;
import com.moujitx.homebox.server.dto.response.DocumentInvoiceResponse;
import com.moujitx.homebox.server.dto.response.DocumentResponse;
import com.moujitx.homebox.server.enums.DocumentStatus;
import com.moujitx.homebox.server.enums.Importance;
import com.moujitx.homebox.server.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public ResponseEntity<Page<DocumentResponse>> getDocuments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) Importance importance,
            @RequestParam(required = false) Boolean parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Boolean parentOnly = parentId == null ? true : parentId;
        return ResponseEntity.ok(documentService.getDocuments(search, categoryId,
                status != null ? status.name() : null,
                importance != null ? importance.name() : null,
                parentOnly, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDetailResponse> getDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @PostMapping
    public ResponseEntity<DocumentDetailResponse> createDocument(@Valid @RequestBody CreateDocumentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.createDocument(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentDetailResponse> updateDocument(@PathVariable Long id,
                                                                  @Valid @RequestBody UpdateDocumentRequest request) {
        return ResponseEntity.ok(documentService.updateDocument(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/invoices")
    public ResponseEntity<List<DocumentInvoiceResponse>> getDocumentInvoices(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentInvoices(id));
    }

    @PostMapping("/{documentId}/invoices/{invoiceId}")
    public ResponseEntity<Void> bindInvoice(@PathVariable Long documentId, @PathVariable Long invoiceId) {
        documentService.bindInvoice(documentId, invoiceId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{documentId}/invoices/{invoiceId}")
    public ResponseEntity<Void> unbindInvoice(@PathVariable Long documentId, @PathVariable Long invoiceId) {
        documentService.unbindInvoice(documentId, invoiceId);
        return ResponseEntity.noContent().build();
    }
}
