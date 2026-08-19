package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.response.GoodPictureResponse;
import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.entity.Good;
import com.moujitx.homebox.server.entity.GoodPicture;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.GoodPictureRepository;
import com.moujitx.homebox.server.repository.GoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoodPictureService {

    private final GoodPictureRepository pictureRepository;
    private final GoodRepository goodRepository;
    private final FileService fileService;

    public List<GoodPictureResponse> getPicturesByGoodId(Long goodId) {
        if (!goodRepository.existsById(goodId)) {
            throw new ResourceNotFoundException("Good not found with id: " + goodId);
        }
        return pictureRepository.findByGoodId(goodId).stream()
                .map(GoodPictureResponse::from)
                .toList();
    }

    @Transactional
    public GoodPictureResponse uploadPicture(Long goodId, MultipartFile file) {
        Good good = goodRepository.findById(goodId)
                .orElseThrow(() -> new ResourceNotFoundException("Good not found with id: " + goodId));

        FileRecord fileRecord = fileService.upload(file);

        GoodPicture picture = new GoodPicture();
        picture.setGood(good);
        picture.setFile(fileRecord);

        return GoodPictureResponse.from(pictureRepository.save(picture));
    }

    @Transactional
    public GoodPictureResponse linkPicture(Long goodId, Long fileId) {
        Good good = goodRepository.findById(goodId)
                .orElseThrow(() -> new ResourceNotFoundException("Good not found with id: " + goodId));

        FileRecord fileRecord = fileService.getFileById(fileId);

        GoodPicture picture = new GoodPicture();
        picture.setGood(good);
        picture.setFile(fileRecord);

        return GoodPictureResponse.from(pictureRepository.save(picture));
    }

    @Transactional
    public List<GoodPictureResponse> sync(Long goodId, List<Long> fileIds) {
        Good good = goodRepository.findById(goodId)
                .orElseThrow(() -> new ResourceNotFoundException("Good not found with id: " + goodId));

        List<Long> desired = fileIds == null ? List.of() : new ArrayList<>(new LinkedHashSet<>(fileIds));
        List<GoodPicture> existing = pictureRepository.findByGoodId(goodId);

        // remove unlisted
        for (GoodPicture p : existing) {
            if (!desired.contains(p.getFile().getId())) {
                Long fileId = p.getFile().getId();
                pictureRepository.delete(p);
                fileService.deleteIfUnused(fileId);
            }
        }

        // link missing
        Set<Long> existingFileIds = existing.stream()
                .map(p -> p.getFile().getId())
                .collect(Collectors.toSet());

        for (Long fileId : desired) {
            if (existingFileIds.contains(fileId)) continue;
            FileRecord fileRecord = fileService.getFileById(fileId);
            GoodPicture picture = new GoodPicture();
            picture.setGood(good);
            picture.setFile(fileRecord);
            pictureRepository.save(picture);
        }

        return pictureRepository.findByGoodId(goodId).stream()
                .map(GoodPictureResponse::from)
                .toList();
    }

    @Transactional
    public void deletePicture(Long goodId, Long pictureId) {
        if (!goodRepository.existsById(goodId)) {
            throw new ResourceNotFoundException("Good not found with id: " + goodId);
        }

        GoodPicture picture = pictureRepository.findById(pictureId)
                .orElseThrow(() -> new ResourceNotFoundException("Picture not found with id: " + pictureId));

        if (!picture.getGood().getId().equals(goodId)) {
            throw new ResourceNotFoundException("Picture not found with id: " + pictureId + " for good: " + goodId);
        }

        Long fileId = picture.getFile().getId();
        pictureRepository.delete(picture);
        fileService.deleteIfUnused(fileId);
    }

    public GoodPicture getPictureEntity(Long goodId, Long pictureId) {
        GoodPicture picture = pictureRepository.findById(pictureId)
                .orElseThrow(() -> new ResourceNotFoundException("Picture not found with id: " + pictureId));

        if (!picture.getGood().getId().equals(goodId)) {
            throw new ResourceNotFoundException("Picture not found with id: " + pictureId + " for good: " + goodId);
        }

        return picture;
    }
}
