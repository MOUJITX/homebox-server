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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public List<AssetPictureResponse> sync(Long assetId, List<Long> fileIds) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + assetId));

        List<Long> desired = fileIds == null ? List.of() : new ArrayList<>(new LinkedHashSet<>(fileIds));
        List<AssetPicture> existing = pictureRepository.findByAssetId(assetId);

        // remove unlisted
        for (AssetPicture p : existing) {
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
            AssetPicture picture = new AssetPicture();
            picture.setAsset(asset);
            picture.setFile(fileRecord);
            pictureRepository.save(picture);
        }

        return pictureRepository.findByAssetId(assetId).stream()
                .map(AssetPictureResponse::from)
                .toList();
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
        fileService.deleteIfUnused(fileId);
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
