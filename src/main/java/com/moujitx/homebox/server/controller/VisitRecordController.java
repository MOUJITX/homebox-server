package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateVisitRecordRequest;
import com.moujitx.homebox.server.dto.request.ParseVisitRecordRequest;
import com.moujitx.homebox.server.dto.request.UpdateVisitRecordRequest;
import com.moujitx.homebox.server.dto.response.VisitRecordParseResponse;
import com.moujitx.homebox.server.dto.response.VisitRecordResponse;
import com.moujitx.homebox.server.enums.VisitType;
import com.moujitx.homebox.server.service.AiService;
import com.moujitx.homebox.server.service.VisitRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/visit-records")
@RequiredArgsConstructor
public class VisitRecordController {

    private final VisitRecordService service;
    private final AiService aiService;

    @GetMapping
    public ResponseEntity<Page<VisitRecordResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) VisitType visitType,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) String patientName) {
        return ResponseEntity.ok(service.list(page, size, visitType, startDate, endDate, institutionId, patientName));
    }

    @GetMapping("/patient-names")
    public ResponseEntity<List<String>> getPatientNames() {
        return ResponseEntity.ok(service.getPatientNames());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VisitRecordResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<VisitRecordResponse> create(@Valid @RequestBody CreateVisitRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VisitRecordResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateVisitRecordRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PostMapping("/parse")
    public ResponseEntity<VisitRecordParseResponse> parse(@Valid @RequestBody ParseVisitRecordRequest request) {
        VisitRecordParseResponse result = aiService.extractVisitRecordInfo(request.getText());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
