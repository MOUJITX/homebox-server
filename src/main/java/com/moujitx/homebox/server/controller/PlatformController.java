package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.PlatformRequest;
import com.moujitx.homebox.server.dto.response.PlatformResponse;
import com.moujitx.homebox.server.service.PlatformService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/platforms")
@RequiredArgsConstructor
public class PlatformController {

    private final PlatformService platformService;

    @GetMapping
    public ResponseEntity<List<PlatformResponse>> getAll() {
        return ResponseEntity.ok(platformService.getAll());
    }

    @PostMapping
    public ResponseEntity<PlatformResponse> create(@Valid @RequestBody PlatformRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(platformService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlatformResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody PlatformRequest request) {
        return ResponseEntity.ok(platformService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        platformService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
