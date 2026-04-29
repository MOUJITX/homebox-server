package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateGoodCategoryRequest;
import com.moujitx.homebox.server.dto.request.UpdateGoodCategoryRequest;
import com.moujitx.homebox.server.dto.response.GoodCategoryResponse;
import com.moujitx.homebox.server.service.GoodCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/good-categories")
public class GoodCategoryController {

    private final GoodCategoryService categoryService;

    public GoodCategoryController(GoodCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<GoodCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
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
