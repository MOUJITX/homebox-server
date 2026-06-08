package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.response.AssetPictureResponse;
import com.moujitx.homebox.server.entity.Asset;
import com.moujitx.homebox.server.entity.AssetPicture;
import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.AssetPictureRepository;
import com.moujitx.homebox.server.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetPictureService {

    private final AssetPictureRepository pictureRepository;
    private final AssetRepository assetRepository;
    private final FileService fileService;

    public List<AssetPictureResponse> getPicturesByAssetId(Long assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found with id: " + assetId);
        }
        return pictureRepository.findByAssetId(assetId).stream()
                .map(AssetPictureResponse::from)
                .toList();
    }

    @Transactional
    public AssetPictureResponse uploadPicture(Long assetId, MultipartFile file) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + assetId));

        FileRecord fileRecord = fileService.upload(file);

        AssetPicture picture = new AssetPicture();
        picture.setAsset(asset);
        picture.setFile(fileRecord);

        return AssetPictureResponse.from(pictureRepository.save(picture));
    }

    @Transactional
    public AssetPictureResponse linkPicture(Long assetId, Long fileId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + assetId));

        FileRecord fileRecord = fileService.getFileById(fileId);

        AssetPicture picture = new AssetPicture();
        picture.setAsset(asset);
        picture.setFile(fileRecord);

        return AssetPictureResponse.from(pictureRepository.save(picture));
    }

    @Transactional
    public void deletePicture(Long assetId, Long pictureId) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found with id: " + assetId);
        }

        AssetPicture picture = pictureRepository.findById(pictureId)
                .orElseThrow(() -> new ResourceNotFoundException("Picture not found with id: " + pictureId));

        if (!picture.getAsset().getId().equals(assetId)) {
            throw new ResourceNotFoundException("Picture not found with id: " + pictureId + " for asset: " + assetId);
        }

        Long fileId = picture.getFile().getId();
        pictureRepository.delete(picture);
        fileService.delete(fileId);
    }

    public AssetPicture getPictureEntity(Long assetId, Long pictureId) {
        AssetPicture picture = pictureRepository.findById(pictureId)
                .orElseThrow(() -> new ResourceNotFoundException("Picture not found with id: " + pictureId));

        if (!picture.getAsset().getId().equals(assetId)) {
            throw new ResourceNotFoundException("Picture not found with id: " + pictureId + " for asset: " + assetId);
        }

        return picture;
    }
}
