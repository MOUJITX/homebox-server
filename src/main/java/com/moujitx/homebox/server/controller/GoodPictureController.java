package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.response.GoodPictureResponse;
import com.moujitx.homebox.server.entity.GoodPicture;
import com.moujitx.homebox.server.service.FileStorageService;
import com.moujitx.homebox.server.service.GoodPictureService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/goods/{goodId}/pictures")
public class GoodPictureController {

    private final GoodPictureService pictureService;
    private final FileStorageService fileStorageService;

    public GoodPictureController(GoodPictureService pictureService,
                                 FileStorageService fileStorageService) {
        this.pictureService = pictureService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping
    public ResponseEntity<GoodPictureResponse> uploadPicture(@PathVariable Long goodId,
                                                             @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pictureService.uploadPicture(goodId, file));
    }

    @DeleteMapping("/{pictureId}")
    public ResponseEntity<Void> deletePicture(@PathVariable Long goodId, @PathVariable Long pictureId) {
        pictureService.deletePicture(goodId, pictureId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{pictureId}/file")
    public ResponseEntity<byte[]> getFile(@PathVariable Long goodId, @PathVariable Long pictureId) {
        GoodPicture picture = pictureService.getPictureEntity(goodId, pictureId);
        byte[] fileData = fileStorageService.load(picture.getFilepath());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(picture.getContentType()));
        headers.setContentLength(fileData.length);

        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }
}
