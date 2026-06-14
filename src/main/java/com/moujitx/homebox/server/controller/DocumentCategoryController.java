package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateDocumentCategoryRequest;
import com.moujitx.homebox.server.dto.request.UpdateDocumentCategoryRequest;
import com.moujitx.homebox.server.dto.response.DocumentCategoryResponse;
import com.moujitx.homebox.server.service.DocumentCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/document-categories")
@RequiredArgsConstructor
public class DocumentCategoryController {

    private final DocumentCategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<DocumentCategoryResponse>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentCategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @PostMapping
    public ResponseEntity<DocumentCategoryResponse> create(@Valid @RequestBody CreateDocumentCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentCategoryResponse> update(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateDocumentCategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
