package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.response.DoubanBookLookupResponse;
import com.moujitx.homebox.server.dto.response.TestConnectionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoubanService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final SystemConfigService systemConfigService;

    private String getApiKey() {
        return systemConfigService.get("douban.api-key");
    }

    private String buildUrl(String baseUrl) {
        String apiKey = getApiKey();
        if (apiKey != null && !apiKey.isEmpty()) {
            String separator = baseUrl.contains("?") ? "&" : "?";
            return baseUrl + separator + "apiKey=" + apiKey;
        }
        return baseUrl;
    }

    @SuppressWarnings("unchecked")
    public DoubanBookLookupResponse lookupByIsbn(String isbn) {
        try {
            String url = buildUrl("https://api.douban.com/v2/book/isbn/" + isbn);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return null;
            }
            return parseBookData(response.getBody());
        } catch (Exception e) {
            log.warn("Douban API lookup failed for ISBN {}: {}", isbn, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public DoubanBookLookupResponse searchBooks(String keyword) {
        try {
            String url = buildUrl("https://api.douban.com/v2/book/search?q=" + keyword + "&count=10");
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return null;
            }
            List<Map<String, Object>> books = (List<Map<String, Object>>) response.getBody().get("books");
            if (books == null || books.isEmpty()) {
                return null;
            }
            if (books.size() == 1) {
                return parseBookData(books.get(0));
            }
            List<DoubanBookLookupResponse> candidates = new ArrayList<>();
            for (Map<String, Object> book : books) {
                candidates.add(parseBookData(book));
            }
            return DoubanBookLookupResponse.list(candidates);
        } catch (Exception e) {
            log.warn("Douban API search failed for keyword {}: {}", keyword, e.getMessage());
            return null;
        }
    }

    public TestConnectionResponse testConnection() {
        try {
            String isbn = "9787559624574";
            DoubanBookLookupResponse result = lookupByIsbn(isbn);
            if (result != null && result.getTitle() != null) {
                return new TestConnectionResponse(true, "Douban API connection successful — found: " + result.getTitle());
            }
            return new TestConnectionResponse(false, "Douban API returned no data");
        } catch (Exception e) {
            return new TestConnectionResponse(false, "Douban API connection failed: " + e.getMessage());
        }
    }

    private String normalizeDate(String pubdate) {
        if (pubdate == null || pubdate.isEmpty()) {
            return null;
        }
        if (pubdate.length() == 7 && pubdate.charAt(4) == '-') {
            return pubdate + "-01";
        }
        if (pubdate.length() == 4 && !pubdate.contains("-")) {
            return pubdate + "-01-01";
        }
        return pubdate;
    }

    @SuppressWarnings("unchecked")
    private DoubanBookLookupResponse parseBookData(Map<String, Object> data) {
        String title = (String) data.get("title");
        List<String> authorList = (List<String>) data.get("author");
        String author = authorList != null ? String.join(", ", authorList) : null;
        String publisher = (String) data.get("publisher");
        String publishDate = normalizeDate((String) data.get("pubdate"));
        String description = (String) data.get("summary");
        String coverUrl = null;
        Map<String, Object> images = (Map<String, Object>) data.get("images");
        if (images != null) {
            coverUrl = (String) images.get("large");
        }
        String isbn = (String) data.get("isbn13");
        if (isbn == null) isbn = (String) data.get("isbn10");

        return DoubanBookLookupResponse.single(title, author, publisher, publishDate, description, coverUrl, isbn);
    }
}
