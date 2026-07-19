package com.moujitx.homebox.server.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class DoubanBookLookupResponse {

    String title;
    String author;
    String publisher;
    String publishDate;
    String description;
    String coverUrl;
    String isbn;

    List<DoubanBookLookupResponse> candidates;

    private DoubanBookLookupResponse() {}

    public static DoubanBookLookupResponse single(String title, String author, String publisher,
                                                   String publishDate, String description,
                                                   String coverUrl, String isbn) {
        DoubanBookLookupResponse r = new DoubanBookLookupResponse();
        r.title = title;
        r.author = author;
        r.publisher = publisher;
        r.publishDate = publishDate;
        r.description = description;
        r.coverUrl = coverUrl;
        r.isbn = isbn;
        return r;
    }

    public static DoubanBookLookupResponse list(List<DoubanBookLookupResponse> candidates) {
        DoubanBookLookupResponse r = new DoubanBookLookupResponse();
        r.candidates = candidates;
        return r;
    }
}
