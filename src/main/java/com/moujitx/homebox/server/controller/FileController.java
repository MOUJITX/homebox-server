package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.response.FileResponse;
import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.service.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
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

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        FileRecord record = fileService.getFileById(id);
        byte[] fileData = fileService.loadFileContent(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(record.getContentType()));
        headers.setContentLength(fileData.length);
        headers.setContentDispositionFormData("attachment", record.getOriginalFilename());

        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
