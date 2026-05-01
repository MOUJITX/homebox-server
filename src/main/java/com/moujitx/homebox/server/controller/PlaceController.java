package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreatePlaceRequest;
import com.moujitx.homebox.server.dto.request.UpdatePlaceRequest;
import com.moujitx.homebox.server.dto.response.PlaceResponse;
import com.moujitx.homebox.server.service.PlaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping
    public ResponseEntity<List<PlaceResponse>> getAllPlaces() {
        return ResponseEntity.ok(placeService.getAllPlaces());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceResponse> getPlaceById(@PathVariable Long id) {
        return ResponseEntity.ok(placeService.getPlaceById(id));
    }

    @PostMapping
    public ResponseEntity<PlaceResponse> createPlace(@Valid @RequestBody CreatePlaceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(placeService.createPlace(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlaceResponse> updatePlace(@PathVariable Long id,
                                                      @Valid @RequestBody UpdatePlaceRequest request) {
        return ResponseEntity.ok(placeService.updatePlace(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlace(@PathVariable Long id) {
        placeService.deletePlace(id);
        return ResponseEntity.noContent().build();
    }
}
