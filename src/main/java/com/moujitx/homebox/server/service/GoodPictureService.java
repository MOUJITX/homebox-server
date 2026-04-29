package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.response.GoodPictureResponse;
import com.moujitx.homebox.server.entity.Good;
import com.moujitx.homebox.server.entity.GoodPicture;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.GoodPictureRepository;
import com.moujitx.homebox.server.repository.GoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class GoodPictureService {

    private final GoodPictureRepository pictureRepository;
    private final GoodRepository goodRepository;
    private final FileStorageService fileStorageService;

    public GoodPictureService(GoodPictureRepository pictureRepository,
                              GoodRepository goodRepository,
                              FileStorageService fileStorageService) {
        this.pictureRepository = pictureRepository;
        this.goodRepository = goodRepository;
        this.fileStorageService = fileStorageService;
    }

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

        String filepath = fileStorageService.store(file, goodId);

        GoodPicture picture = new GoodPicture();
        picture.setGood(good);
        picture.setFilename(file.getOriginalFilename());
        picture.setFilepath(filepath);
        picture.setContentType(file.getContentType());
        picture.setFileSize(file.getSize());

        return GoodPictureResponse.from(pictureRepository.save(picture));
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

        fileStorageService.delete(picture.getFilepath());
        pictureRepository.delete(picture);
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
