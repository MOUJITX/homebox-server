package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateGoodItemRequest;
import com.moujitx.homebox.server.dto.request.UpdateGoodItemRequest;
import com.moujitx.homebox.server.dto.response.GoodItemResponse;
import com.moujitx.homebox.server.service.GoodItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goods/{goodId}/items")
public class GoodItemController {

    private final GoodItemService itemService;

    public GoodItemController(GoodItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<GoodItemResponse>> getItems(@PathVariable Long goodId) {
        return ResponseEntity.ok(itemService.getItemsByGoodId(goodId));
    }

    @PostMapping
    public ResponseEntity<GoodItemResponse> createItem(@PathVariable Long goodId,
                                                       @Valid @RequestBody CreateGoodItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.createItem(goodId, request));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<GoodItemResponse> updateItem(@PathVariable Long goodId,
                                                       @PathVariable Long itemId,
                                                       @Valid @RequestBody UpdateGoodItemRequest request) {
        return ResponseEntity.ok(itemService.updateItem(goodId, itemId, request));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long goodId, @PathVariable Long itemId) {
        itemService.deleteItem(goodId, itemId);
        return ResponseEntity.noContent().build();
    }
}
