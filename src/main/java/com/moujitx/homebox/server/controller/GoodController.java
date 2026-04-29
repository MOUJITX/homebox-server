package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateGoodRequest;
import com.moujitx.homebox.server.dto.request.UpdateGoodRequest;
import com.moujitx.homebox.server.dto.response.GoodDetailResponse;
import com.moujitx.homebox.server.dto.response.GoodResponse;
import com.moujitx.homebox.server.enums.GoodStatus;
import com.moujitx.homebox.server.service.GoodService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goods")
public class GoodController {

    private final GoodService goodService;

    public GoodController(GoodService goodService) {
        this.goodService = goodService;
    }

    @GetMapping
    public ResponseEntity<Page<GoodResponse>> getGoods(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) GoodStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(goodService.getGoods(search, categoryId, brandId, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoodDetailResponse> getGoodById(@PathVariable Long id) {
        return ResponseEntity.ok(goodService.getGoodById(id));
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<GoodResponse> getGoodByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(goodService.getGoodByBarcode(barcode));
    }

    @PostMapping
    public ResponseEntity<GoodDetailResponse> createGood(@Valid @RequestBody CreateGoodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goodService.createGood(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoodDetailResponse> updateGood(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateGoodRequest request) {
        return ResponseEntity.ok(goodService.updateGood(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGood(@PathVariable Long id) {
        goodService.deleteGood(id);
        return ResponseEntity.noContent().build();
    }
}
