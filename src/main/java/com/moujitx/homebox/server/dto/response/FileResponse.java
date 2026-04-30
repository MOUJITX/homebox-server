package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.FileRecord;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
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
}
