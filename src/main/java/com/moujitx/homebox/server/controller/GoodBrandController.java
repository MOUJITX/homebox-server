package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateGoodBrandRequest;
import com.moujitx.homebox.server.dto.request.UpdateGoodBrandRequest;
import com.moujitx.homebox.server.dto.response.GoodBrandResponse;
import com.moujitx.homebox.server.service.GoodBrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/good-brands")
@RequiredArgsConstructor
public class GoodBrandController {

    private final GoodBrandService brandService;

    @GetMapping
    public ResponseEntity<List<GoodBrandResponse>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoodBrandResponse> getBrandById(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.getBrandById(id));
    }

    @PostMapping
    public ResponseEntity<GoodBrandResponse> createBrand(@Valid @RequestBody CreateGoodBrandRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(brandService.createBrand(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoodBrandResponse> updateBrand(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateGoodBrandRequest request) {
        return ResponseEntity.ok(brandService.updateBrand(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
}
