package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.response.FileResponse;
import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping
    public Page<FileResponse> listFiles(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return fileService.listFiles(PageRequest.of(page, size))
                .map(FileResponse::from);
    }

    @PostMapping
    public ResponseEntity<FileResponse> upload(@RequestParam("file") MultipartFile file) {
        FileRecord record = fileService.upload(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(FileResponse.from(record));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileResponse> getFile(@PathVariable Long id) {
        FileRecord record = fileService.getFileById(id);
        return ResponseEntity.ok(FileResponse.from(record));
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<byte[]> preview(@PathVariable Long id) {
        FileRecord record = fileService.getFileById(id);
        byte[] fileData = fileService.loadFileContent(record);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(record.getContentType()));
        headers.setContentLength(fileData.length);

        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        FileRecord record = fileService.getFileById(id);
        byte[] fileData = fileService.loadFileContent(record);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(record.getContentType()));
        headers.setContentLength(fileData.length);
        headers.setContentDispositionFormData("attachment", record.getOriginalFilename());

        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }

    @PatchMapping("/{id}/rename")
    public ResponseEntity<FileResponse> rename(@PathVariable Long id,
                                                @RequestBody Map<String, String> body) {
        String originalFilename = body.get("originalFilename");
        if (originalFilename == null || originalFilename.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        FileRecord record = fileService.rename(id, originalFilename.trim());
        return ResponseEntity.ok(FileResponse.from(record));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
