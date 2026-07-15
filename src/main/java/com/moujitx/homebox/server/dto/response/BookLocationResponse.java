package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.BookLocation;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BookLocationResponse {

    Long id;
    String name;
    String description;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static BookLocationResponse from(BookLocation location) {
        BookLocationResponse r = new BookLocationResponse();
        r.id = location.getId();
        r.name = location.getName();
        r.description = location.getDescription();
        r.createdAt = location.getCreatedAt();
        r.updatedAt = location.getUpdatedAt();
        return r;
    }
}
