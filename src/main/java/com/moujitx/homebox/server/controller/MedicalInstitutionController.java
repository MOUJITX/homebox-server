package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.MedicalInstitutionRequest;
import com.moujitx.homebox.server.dto.response.MedicalInstitutionResponse;
import com.moujitx.homebox.server.service.MedicalInstitutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-institutions")
@RequiredArgsConstructor
public class MedicalInstitutionController {

    private final MedicalInstitutionService service;

    @GetMapping
    public ResponseEntity<List<MedicalInstitutionResponse>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<MedicalInstitutionResponse>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(service.page(page, size, name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalInstitutionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<MedicalInstitutionResponse> create(@Valid @RequestBody MedicalInstitutionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicalInstitutionResponse> update(@PathVariable Long id,
                                                              @Valid @RequestBody MedicalInstitutionRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
