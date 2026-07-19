package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateBookLocationRequest;
import com.moujitx.homebox.server.dto.request.UpdateBookLocationRequest;
import com.moujitx.homebox.server.dto.response.BookLocationResponse;
import com.moujitx.homebox.server.service.BookLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book-locations")
@RequiredArgsConstructor
public class BookLocationController {

    private final BookLocationService bookLocationService;

    @GetMapping
    public ResponseEntity<List<BookLocationResponse>> getAllLocations() {
        return ResponseEntity.ok(bookLocationService.getAllLocations());
    }

    @PostMapping
    public ResponseEntity<BookLocationResponse> createLocation(@Valid @RequestBody CreateBookLocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookLocationService.createLocation(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookLocationResponse> updateLocation(@PathVariable Long id,
                                                                @Valid @RequestBody UpdateBookLocationRequest request) {
        return ResponseEntity.ok(bookLocationService.updateLocation(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        bookLocationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }
}
