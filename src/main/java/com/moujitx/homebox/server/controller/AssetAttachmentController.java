package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.SyncFileIdsRequest;
import com.moujitx.homebox.server.dto.response.AssetAttachmentResponse;
import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.service.AssetAttachmentService;
import com.moujitx.homebox.server.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/assets/{assetId}/attachments")
@RequiredArgsConstructor
public class AssetAttachmentController {

    private final AssetAttachmentService assetAttachmentService;
    private final FileService fileService;

    @GetMapping
    public ResponseEntity<List<AssetAttachmentResponse>> getAttachments(@PathVariable Long assetId) {
        return ResponseEntity.ok(assetAttachmentService.getByAssetId(assetId));
    }

    @PutMapping
    public ResponseEntity<List<AssetAttachmentResponse>> syncAttachments(@PathVariable Long assetId,
                                                                         @RequestBody SyncFileIdsRequest body) {
        return ResponseEntity.ok(assetAttachmentService.sync(assetId, body.getFileIds()));
    }

    @PostMapping
    public ResponseEntity<AssetAttachmentResponse> uploadAttachment(@PathVariable Long assetId,
                                                                     @RequestParam(value = "file", required = false) MultipartFile file,
                                                                     @RequestParam(value = "fileId", required = false) Long fileId) {
        if (fileId != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(assetAttachmentService.link(assetId, fileId));
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(assetAttachmentService.upload(assetId, file));
    }

    @GetMapping("/{attachmentId}/file")
    public ResponseEntity<byte[]> getFile(@PathVariable Long assetId, @PathVariable Long attachmentId) {
        FileRecord file = assetAttachmentService.getAttachmentFile(assetId, attachmentId);
        byte[] data = fileService.loadFileContent(file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));
        headers.setContentLength(data.length);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long assetId, @PathVariable Long attachmentId) {
        assetAttachmentService.delete(assetId, attachmentId);
        return ResponseEntity.noContent().build();
    }
}
