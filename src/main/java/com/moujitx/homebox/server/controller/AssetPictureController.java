package com.moujitx.homebox.server.controller;

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

@RestController
@RequestMapping("/api/assets/{assetId}/pictures")
@RequiredArgsConstructor
public class AssetPictureController {

    private final AssetPictureService pictureService;
    private final FileService fileService;

    @PostMapping
    public ResponseEntity<AssetPictureResponse> uploadPicture(@PathVariable Long assetId,
                                                               @RequestParam("file") MultipartFile file) {
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
