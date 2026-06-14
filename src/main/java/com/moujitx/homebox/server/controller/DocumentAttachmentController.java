package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.response.DocumentAttachmentResponse;
import com.moujitx.homebox.server.service.DocumentAttachmentService;
import com.moujitx.homebox.server.service.FileService;
import com.moujitx.homebox.server.entity.FileRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents/{documentId}/attachments")
@RequiredArgsConstructor
public class DocumentAttachmentController {

    private final DocumentAttachmentService attachmentService;
    private final FileService fileService;

    @GetMapping
    public ResponseEntity<List<DocumentAttachmentResponse>> getAttachments(@PathVariable Long documentId) {
        return ResponseEntity.ok(attachmentService.getByDocumentId(documentId));
    }

    @PostMapping
    public ResponseEntity<DocumentAttachmentResponse> uploadAttachment(@PathVariable Long documentId,
                                                                        @RequestParam(value = "file", required = false) MultipartFile file,
                                                                        @RequestParam(value = "fileId", required = false) Long fileId) {
        if (fileId != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(attachmentService.link(documentId, fileId));
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(attachmentService.upload(documentId, file));
    }

    @GetMapping("/{attachmentId}/file")
    public ResponseEntity<byte[]> getFile(@PathVariable Long documentId, @PathVariable Long attachmentId) {
        FileRecord file = attachmentService.getAttachmentFile(documentId, attachmentId);
        byte[] data = fileService.loadFileContent(file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));
        headers.setContentLength(data.length);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long documentId, @PathVariable Long attachmentId) {
        attachmentService.delete(documentId, attachmentId);
        return ResponseEntity.noContent().build();
    }
}
