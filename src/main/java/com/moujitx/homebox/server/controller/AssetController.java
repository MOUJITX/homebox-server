package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateAssetRequest;
import com.moujitx.homebox.server.dto.request.UpdateAssetRequest;
import com.moujitx.homebox.server.dto.response.AssetDetailResponse;
import com.moujitx.homebox.server.dto.response.AssetResponse;
import com.moujitx.homebox.server.enums.WarrantyStatus;
import com.moujitx.homebox.server.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    public ResponseEntity<Page<AssetResponse>> getAssets(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long placeId,
            @RequestParam(required = false) Boolean isInUse,
            @RequestParam(required = false) WarrantyStatus warrantyStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(assetService.getAssets(search, categoryId, placeId, isInUse, warrantyStatus, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetDetailResponse> getAssetById(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.getAssetById(id));
    }

    @PostMapping
    public ResponseEntity<AssetDetailResponse> createAsset(@Valid @RequestBody CreateAssetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.createAsset(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetDetailResponse> updateAsset(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateAssetRequest request) {
        return ResponseEntity.ok(assetService.updateAsset(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }
}
