package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.BindInvoiceRequest;
import com.moujitx.homebox.server.dto.response.VisitInvoiceResponse;
import com.moujitx.homebox.server.service.VisitInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visit-records/{visitId}/invoices")
@RequiredArgsConstructor
public class VisitInvoiceController {

    private final VisitInvoiceService service;

    @GetMapping
    public ResponseEntity<List<VisitInvoiceResponse>> list(@PathVariable Long visitId) {
        return ResponseEntity.ok(service.list(visitId));
    }

    @PostMapping
    public ResponseEntity<VisitInvoiceResponse> bind(@PathVariable Long visitId,
                                                       @Valid @RequestBody BindInvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.bind(visitId, request.getInvoiceId(), request.getSourceType(), request.getSourceId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unbind(@PathVariable Long id) {
        service.unbind(id);
        return ResponseEntity.noContent().build();
    }
}
