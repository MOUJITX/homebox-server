package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateInvoiceRequest;
import com.moujitx.homebox.server.dto.request.UpdateInvoiceRequest;
import com.moujitx.homebox.server.dto.response.InvoiceDetailResponse;
import com.moujitx.homebox.server.dto.response.InvoiceParseResponse;
import com.moujitx.homebox.server.dto.response.InvoiceResponse;
import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.enums.InvoiceStatus;
import com.moujitx.homebox.server.enums.InvoiceType;
import com.moujitx.homebox.server.service.FileService;
import com.moujitx.homebox.server.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final FileService fileService;

    @GetMapping
    public ResponseEntity<Page<InvoiceResponse>> getInvoices(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) InvoiceType invoiceType,
            @RequestParam(required = false) InvoiceStatus invoiceStatus,
            @RequestParam(required = false) String buyerName,
            @RequestParam(required = false) String sellerName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(invoiceService.getInvoices(search, invoiceType, invoiceStatus, buyerName, sellerName, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDetailResponse> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @PostMapping
    public ResponseEntity<InvoiceDetailResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.createInvoice(request));
    }

    @PostMapping("/parse")
    public ResponseEntity<InvoiceParseResponse> parseInvoice(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(invoiceService.parseInvoice(file));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceDetailResponse> updateInvoice(@PathVariable Long id,
                                                                @Valid @RequestBody UpdateInvoiceRequest request) {
        return ResponseEntity.ok(invoiceService.updateInvoice(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/file/preview")
    public ResponseEntity<byte[]> previewFile(@PathVariable Long id) {
        FileRecord file = invoiceService.getInvoiceFile(id);
        byte[] data = fileService.loadFileContent(file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));
        headers.setContentLength(data.length);
        headers.set("Content-Disposition", "inline; filename=\"" + file.getOriginalFilename() + "\"");

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}/file/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        FileRecord file = invoiceService.getInvoiceFile(id);
        byte[] data = fileService.loadFileContent(file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));
        headers.setContentLength(data.length);
        headers.set("Content-Disposition", "attachment; filename=\"" + file.getOriginalFilename() + "\"");

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
