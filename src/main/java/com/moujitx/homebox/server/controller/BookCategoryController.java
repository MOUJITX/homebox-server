package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateBookCategoryRequest;
import com.moujitx.homebox.server.dto.request.UpdateBookCategoryRequest;
import com.moujitx.homebox.server.dto.response.BookCategoryResponse;
import com.moujitx.homebox.server.service.BookCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book-categories")
@RequiredArgsConstructor
public class BookCategoryController {

    private final BookCategoryService bookCategoryService;

    @GetMapping
    public ResponseEntity<List<BookCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(bookCategoryService.getAllCategories());
    }

    @PostMapping
    public ResponseEntity<BookCategoryResponse> createCategory(@Valid @RequestBody CreateBookCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookCategoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookCategoryResponse> updateCategory(@PathVariable Long id,
                                                                @Valid @RequestBody UpdateBookCategoryRequest request) {
        return ResponseEntity.ok(bookCategoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        bookCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
