package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreatePrescriptionItemRequest;
import com.moujitx.homebox.server.dto.response.PrescriptionItemResponse;
import com.moujitx.homebox.server.service.VisitPrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visit-records/{visitId}/prescriptions/{prescriptionId}/items")
@RequiredArgsConstructor
public class PrescriptionItemController {

    private final VisitPrescriptionService service;

    @PostMapping
    public ResponseEntity<PrescriptionItemResponse> create(@PathVariable Long prescriptionId,
                                                            @Valid @RequestBody CreatePrescriptionItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addItem(prescriptionId, request));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<PrescriptionItemResponse> update(@PathVariable Long itemId,
                                                            @Valid @RequestBody CreatePrescriptionItemRequest request) {
        return ResponseEntity.ok(service.updateItem(itemId, request));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(@PathVariable Long itemId) {
        service.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }
}
