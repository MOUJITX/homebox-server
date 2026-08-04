package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateGoodCategoryRequest;
import com.moujitx.homebox.server.dto.request.UpdateGoodCategoryRequest;
import com.moujitx.homebox.server.dto.response.GoodCategoryResponse;
import com.moujitx.homebox.server.service.GoodCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/good-categories")
@RequiredArgsConstructor
public class GoodCategoryController {

    private final GoodCategoryService categoryService;

    @GetMapping
    public ResponseEntity<Page<GoodCategoryResponse>> getAllCategories(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable = (page != null && size != null)
                ? PageRequest.of(page, size)
                : Pageable.unpaged();
        return ResponseEntity.ok(categoryService.getAllCategories(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoodCategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping
    public ResponseEntity<GoodCategoryResponse> createCategory(@Valid @RequestBody CreateGoodCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoodCategoryResponse> updateCategory(@PathVariable Long id,
                                                               @Valid @RequestBody UpdateGoodCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
