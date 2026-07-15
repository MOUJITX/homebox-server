package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.BookSeries;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BookSeriesResponse {

    Long id;
    String name;
    String description;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static BookSeriesResponse from(BookSeries series) {
        BookSeriesResponse r = new BookSeriesResponse();
        r.id = series.getId();
        r.name = series.getName();
        r.description = series.getDescription();
        r.createdAt = series.getCreatedAt();
        r.updatedAt = series.getUpdatedAt();
        return r;
    }
}
