package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.response.GoodPictureResponse;
import com.moujitx.homebox.server.entity.GoodPicture;
import com.moujitx.homebox.server.service.FileService;
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
    private final FileService fileService;

    public GoodPictureController(GoodPictureService pictureService,
                                 FileService fileService) {
        this.pictureService = pictureService;
        this.fileService = fileService;
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
        byte[] fileData = fileService.loadFileContent(picture.getFile().getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(picture.getFile().getContentType()));
        headers.setContentLength(fileData.length);

        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }
}
