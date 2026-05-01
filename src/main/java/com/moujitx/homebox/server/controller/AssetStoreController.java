package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateAssetStoreRequest;
import com.moujitx.homebox.server.dto.request.UpdateAssetStoreRequest;
import com.moujitx.homebox.server.dto.response.AssetStoreResponse;
import com.moujitx.homebox.server.service.AssetStoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asset-stores")
@RequiredArgsConstructor
public class AssetStoreController {

    private final AssetStoreService assetStoreService;

    @GetMapping
    public ResponseEntity<List<AssetStoreResponse>> getAllStores() {
        return ResponseEntity.ok(assetStoreService.getAllStores());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetStoreResponse> getStoreById(@PathVariable Long id) {
        return ResponseEntity.ok(assetStoreService.getStoreById(id));
    }

    @PostMapping
    public ResponseEntity<AssetStoreResponse> createStore(@Valid @RequestBody CreateAssetStoreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetStoreService.createStore(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetStoreResponse> updateStore(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateAssetStoreRequest request) {
        return ResponseEntity.ok(assetStoreService.updateStore(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        assetStoreService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }
}
