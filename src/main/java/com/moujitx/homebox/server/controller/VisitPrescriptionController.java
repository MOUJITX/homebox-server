package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreatePrescriptionRequest;
import com.moujitx.homebox.server.dto.response.VisitPrescriptionResponse;
import com.moujitx.homebox.server.service.VisitPrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visit-records/{visitId}/prescriptions")
@RequiredArgsConstructor
public class VisitPrescriptionController {

    private final VisitPrescriptionService service;

    @GetMapping
    public ResponseEntity<Page<VisitPrescriptionResponse>> list(
            @PathVariable Long visitId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.list(visitId, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VisitPrescriptionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<VisitPrescriptionResponse> create(@PathVariable Long visitId,
                                                             @Valid @RequestBody CreatePrescriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(visitId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VisitPrescriptionResponse> update(@PathVariable Long id,
                                                             @Valid @RequestBody CreatePrescriptionRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
