package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.FileRecord;

import java.time.LocalDateTime;

public class FileResponse {

    private Long id;
    private String originalFilename;
    private String contentType;
    private long fileSize;
    private String url;
    private LocalDateTime createdAt;

    public static FileResponse from(FileRecord record) {
        FileResponse response = new FileResponse();
        response.id = record.getId();
        response.originalFilename = record.getOriginalFilename();
        response.contentType = record.getContentType();
        response.fileSize = record.getFileSize();
        response.url = "/api/files/" + record.getId() + "/download";
        response.createdAt = record.getCreatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getUrl() {
        return url;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
