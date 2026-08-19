package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.SyncFileIdsRequest;
import com.moujitx.homebox.server.dto.response.AssetPictureResponse;
import com.moujitx.homebox.server.entity.AssetPicture;
import com.moujitx.homebox.server.service.AssetPictureService;
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
@RequestMapping("/api/assets/{assetId}/pictures")
@RequiredArgsConstructor
public class AssetPictureController {

    private final AssetPictureService pictureService;
    private final FileService fileService;

    @PutMapping
    public ResponseEntity<List<AssetPictureResponse>> syncPictures(@PathVariable Long assetId,
                                                                   @RequestBody SyncFileIdsRequest body) {
        return ResponseEntity.ok(pictureService.sync(assetId, body.getFileIds()));
    }

    @PostMapping
    public ResponseEntity<AssetPictureResponse> uploadPicture(@PathVariable Long assetId,
                                                               @RequestParam(value = "file", required = false) MultipartFile file,
                                                               @RequestParam(value = "fileId", required = false) Long fileId) {
        if (fileId != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(pictureService.linkPicture(assetId, fileId));
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(pictureService.uploadPicture(assetId, file));
    }

    @DeleteMapping("/{pictureId}")
    public ResponseEntity<Void> deletePicture(@PathVariable Long assetId, @PathVariable Long pictureId) {
        pictureService.deletePicture(assetId, pictureId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{pictureId}/file")
    public ResponseEntity<byte[]> getFile(@PathVariable Long assetId, @PathVariable Long pictureId) {
        AssetPicture picture = pictureService.getPictureEntity(assetId, pictureId);
        byte[] fileData = fileService.loadFileContent(picture.getFile().getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(picture.getFile().getContentType()));
        headers.setContentLength(fileData.length);

        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }
}
