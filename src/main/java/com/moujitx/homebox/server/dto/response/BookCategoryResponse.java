package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.BookCategory;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BookCategoryResponse {

    Long id;
    String name;
    String key;
    boolean serialized;
    String description;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static BookCategoryResponse from(BookCategory category) {
        BookCategoryResponse r = new BookCategoryResponse();
        r.id = category.getId();
        r.name = category.getName();
        r.key = category.getKey();
        r.serialized = category.isSerialized();
        r.description = category.getDescription();
        r.createdAt = category.getCreatedAt();
        r.updatedAt = category.getUpdatedAt();
        return r;
    }
}
