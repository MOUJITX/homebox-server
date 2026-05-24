package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateMedicationReminderRequest;
import com.moujitx.homebox.server.dto.request.UpdateMedicationReminderRequest;
import com.moujitx.homebox.server.dto.response.MedicationReminderResponse;
import com.moujitx.homebox.server.service.MedicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    @GetMapping
    public ResponseEntity<Page<MedicationReminderResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "courseStartDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Page<MedicationReminderResponse> result = medicationService.list(
                PageRequest.of(page, size, Sort.by(direction, sortBy)), enabled);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicationReminderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(medicationService.getById(id));
    }

    @PostMapping
    public ResponseEntity<MedicationReminderResponse> create(@Valid @RequestBody CreateMedicationReminderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicationService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicationReminderResponse> update(@PathVariable Long id,
                                                              @Valid @RequestBody UpdateMedicationReminderRequest request) {
        return ResponseEntity.ok(medicationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        medicationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
