package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.response.SearchResultItem;
import com.moujitx.homebox.server.service.EsClientProvider;
import com.moujitx.homebox.server.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final EsClientProvider esClientProvider;

    @GetMapping
    public ResponseEntity<Page<SearchResultItem>> search(
            @RequestParam("q") String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(searchService.search(q, page, size));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> status() {
        return ResponseEntity.ok(Map.of("available", esClientProvider.isSearchEnabled()));
    }
}
