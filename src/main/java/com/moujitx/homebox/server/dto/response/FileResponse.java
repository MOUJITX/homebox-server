package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.enums.ProcessStatus;
import com.moujitx.homebox.server.util.OssUrlBuilder;
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
    private boolean indexed;
    private ProcessStatus extractStatus;
    private ProcessStatus chunkStatus;

    public static FileResponse from(FileRecord record) {
        return from(record, true);
    }

    public static FileResponse from(FileRecord record, boolean indexed) {
        FileResponse response = new FileResponse();
        response.id = record.getId();
        response.storedFilename = record.getStoredFilename();
        response.originalFilename = record.getOriginalFilename();
        response.contentType = record.getContentType();
        response.fileSize = record.getFileSize();
        response.url = OssUrlBuilder.build(record.getStoredFilename(), record.getOriginalFilename());
        response.createdAt = record.getCreatedAt();
        response.indexed = indexed;
        response.extractStatus = record.getExtractStatus();
        response.chunkStatus = record.getChunkStatus();
        return response;
    }
}
