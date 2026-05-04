package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.FileRecord;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FileResponse {

    private Long id;
    private String storedFilename;
    private String originalFilename;
    private String contentType;
    private long fileSize;
    private String url;
    private LocalDateTime createdAt;

    public static FileResponse from(FileRecord record) {
        FileResponse response = new FileResponse();
        response.id = record.getId();
        response.storedFilename = record.getStoredFilename();
        response.originalFilename = record.getOriginalFilename();
        response.contentType = record.getContentType();
        response.fileSize = record.getFileSize();
        response.url = "/oss/" + record.getStoredFilename();
        response.createdAt = record.getCreatedAt();
        return response;
    }
}
