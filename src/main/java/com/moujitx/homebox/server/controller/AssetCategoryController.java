package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateAssetCategoryRequest;
import com.moujitx.homebox.server.dto.request.UpdateAssetCategoryRequest;
import com.moujitx.homebox.server.dto.response.AssetCategoryResponse;
import com.moujitx.homebox.server.service.AssetCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asset-categories")
@RequiredArgsConstructor
public class AssetCategoryController {

    private final AssetCategoryService assetCategoryService;

    @GetMapping
    public ResponseEntity<List<AssetCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(assetCategoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetCategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(assetCategoryService.getCategoryById(id));
    }

    @PostMapping
    public ResponseEntity<AssetCategoryResponse> createCategory(@Valid @RequestBody CreateAssetCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetCategoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetCategoryResponse> updateCategory(@PathVariable Long id,
                                                                 @Valid @RequestBody UpdateAssetCategoryRequest request) {
        return ResponseEntity.ok(assetCategoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        assetCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
