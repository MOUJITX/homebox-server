package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.DocumentCategory;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DocumentCategoryResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DocumentCategoryResponse from(DocumentCategory category) {
        DocumentCategoryResponse response = new DocumentCategoryResponse();
        response.id = category.getId();
        response.name = category.getName();
        response.description = category.getDescription();
        response.createdAt = category.getCreatedAt();
        response.updatedAt = category.getUpdatedAt();
        return response;
    }
}
