package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateExaminationRequest;
import com.moujitx.homebox.server.dto.response.VisitExaminationResponse;
import com.moujitx.homebox.server.service.VisitExaminationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visit-records/{visitId}/examinations")
@RequiredArgsConstructor
public class VisitExaminationController {

    private final VisitExaminationService service;

    @GetMapping
    public ResponseEntity<Page<VisitExaminationResponse>> list(
            @PathVariable Long visitId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.list(visitId, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VisitExaminationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<VisitExaminationResponse> create(@PathVariable Long visitId,
                                                            @Valid @RequestBody CreateExaminationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(visitId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VisitExaminationResponse> update(@PathVariable Long id,
                                                            @Valid @RequestBody CreateExaminationRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
