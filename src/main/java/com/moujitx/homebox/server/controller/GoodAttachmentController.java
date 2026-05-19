package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.response.GoodAttachmentResponse;
import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.service.FileService;
import com.moujitx.homebox.server.service.GoodAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/goods/{goodId}/attachments")
@RequiredArgsConstructor
public class GoodAttachmentController {

    private final GoodAttachmentService goodAttachmentService;
    private final FileService fileService;

    @GetMapping
    public ResponseEntity<java.util.List<GoodAttachmentResponse>> getAttachments(@PathVariable Long goodId) {
        return ResponseEntity.ok(goodAttachmentService.getByGoodId(goodId));
    }

    @PostMapping
    public ResponseEntity<GoodAttachmentResponse> uploadAttachment(@PathVariable Long goodId,
                                                                    @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goodAttachmentService.upload(goodId, file));
    }

    @GetMapping("/{attachmentId}/file")
    public ResponseEntity<byte[]> getFile(@PathVariable Long goodId, @PathVariable Long attachmentId) {
        FileRecord file = goodAttachmentService.getAttachmentFile(goodId, attachmentId);
        byte[] data = fileService.loadFileContent(file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));
        headers.setContentLength(data.length);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long goodId, @PathVariable Long attachmentId) {
        goodAttachmentService.delete(goodId, attachmentId);
        return ResponseEntity.noContent().build();
    }
}
