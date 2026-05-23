package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.response.VisitAttachmentResponse;
import com.moujitx.homebox.server.enums.VisitSourceType;
import com.moujitx.homebox.server.service.VisitAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/visit-records/{visitId}/attachments")
@RequiredArgsConstructor
public class VisitAttachmentController {

    private final VisitAttachmentService service;

    @GetMapping
    public ResponseEntity<List<VisitAttachmentResponse>> list(@PathVariable Long visitId) {
        return ResponseEntity.ok(service.list(visitId));
    }

    @PostMapping
    public ResponseEntity<VisitAttachmentResponse> upload(@PathVariable Long visitId,
                                                            @RequestParam("file") MultipartFile file,
                                                            @RequestParam VisitSourceType sourceType,
                                                            @RequestParam Long sourceId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.upload(visitId, file, sourceType, sourceId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
