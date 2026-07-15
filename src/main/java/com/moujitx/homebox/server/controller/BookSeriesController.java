package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateBookSeriesRequest;
import com.moujitx.homebox.server.dto.request.UpdateBookSeriesRequest;
import com.moujitx.homebox.server.dto.response.BookSeriesResponse;
import com.moujitx.homebox.server.service.BookSeriesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book-series")
@RequiredArgsConstructor
public class BookSeriesController {

    private final BookSeriesService bookSeriesService;

    @GetMapping
    public ResponseEntity<List<BookSeriesResponse>> getAllSeries() {
        return ResponseEntity.ok(bookSeriesService.getAllSeries());
    }

    @PostMapping
    public ResponseEntity<BookSeriesResponse> createSeries(@Valid @RequestBody CreateBookSeriesRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookSeriesService.createSeries(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookSeriesResponse> updateSeries(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateBookSeriesRequest request) {
        return ResponseEntity.ok(bookSeriesService.updateSeries(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeries(@PathVariable Long id) {
        bookSeriesService.deleteSeries(id);
        return ResponseEntity.noContent().build();
    }
}
