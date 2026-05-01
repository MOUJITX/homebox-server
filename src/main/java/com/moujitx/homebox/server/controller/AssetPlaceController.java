package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateAssetPlaceRequest;
import com.moujitx.homebox.server.dto.request.UpdateAssetPlaceRequest;
import com.moujitx.homebox.server.dto.response.AssetPlaceResponse;
import com.moujitx.homebox.server.service.AssetPlaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asset-places")
@RequiredArgsConstructor
public class AssetPlaceController {

    private final AssetPlaceService assetPlaceService;

    @GetMapping
    public ResponseEntity<List<AssetPlaceResponse>> getAllPlaces() {
        return ResponseEntity.ok(assetPlaceService.getAllPlaces());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetPlaceResponse> getPlaceById(@PathVariable Long id) {
        return ResponseEntity.ok(assetPlaceService.getPlaceById(id));
    }

    @PostMapping
    public ResponseEntity<AssetPlaceResponse> createPlace(@Valid @RequestBody CreateAssetPlaceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetPlaceService.createPlace(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetPlaceResponse> updatePlace(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateAssetPlaceRequest request) {
        return ResponseEntity.ok(assetPlaceService.updatePlace(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlace(@PathVariable Long id) {
        assetPlaceService.deletePlace(id);
        return ResponseEntity.noContent().build();
    }
}
