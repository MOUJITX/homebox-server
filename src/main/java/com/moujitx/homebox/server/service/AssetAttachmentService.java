package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.response.AssetAttachmentResponse;
import com.moujitx.homebox.server.entity.Asset;
import com.moujitx.homebox.server.entity.AssetAttachment;
import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.AssetAttachmentRepository;
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
public class AssetAttachmentService {

    private final AssetAttachmentRepository attachmentRepository;
    private final AssetRepository assetRepository;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public List<AssetAttachmentResponse> getByAssetId(Long assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found with id: " + assetId);
        }
        return attachmentRepository.findByAssetId(assetId).stream()
                .map(a -> AssetAttachmentResponse.from(a, fileService.isIndexed(a.getFile().getId())))
                .toList();
    }

    @Transactional
    public AssetAttachmentResponse upload(Long assetId, MultipartFile file) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + assetId));

        FileRecord fileRecord = fileService.upload(file);

        AssetAttachment attachment = new AssetAttachment();
        attachment.setAsset(asset);
        attachment.setFile(fileRecord);

        attachment = attachmentRepository.save(attachment);
        return AssetAttachmentResponse.from(attachment, false);
    }

    @Transactional
    public AssetAttachmentResponse link(Long assetId, Long fileId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + assetId));

        FileRecord fileRecord = fileService.getFileById(fileId);

        AssetAttachment attachment = new AssetAttachment();
        attachment.setAsset(asset);
        attachment.setFile(fileRecord);

        return AssetAttachmentResponse.from(attachmentRepository.save(attachment), fileService.isIndexed(fileId));
    }

    @Transactional
    public List<AssetAttachmentResponse> sync(Long assetId, List<Long> fileIds) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + assetId));

        List<Long> desired = fileIds == null ? List.of() : new ArrayList<>(new LinkedHashSet<>(fileIds));
        List<AssetAttachment> existing = attachmentRepository.findByAssetId(assetId);

        // remove unlisted
        for (AssetAttachment a : existing) {
            if (!desired.contains(a.getFile().getId())) {
                Long fileId = a.getFile().getId();
                attachmentRepository.delete(a);
                fileService.deleteIfUnused(fileId);
            }
        }

        // link missing
        Set<Long> existingFileIds = existing.stream()
                .map(a -> a.getFile().getId())
                .collect(Collectors.toSet());

        for (Long fileId : desired) {
            if (existingFileIds.contains(fileId)) continue;
            FileRecord fileRecord = fileService.getFileById(fileId);
            AssetAttachment attachment = new AssetAttachment();
            attachment.setAsset(asset);
            attachment.setFile(fileRecord);
            attachmentRepository.save(attachment);
        }

        return attachmentRepository.findByAssetId(assetId).stream()
                .map(a -> AssetAttachmentResponse.from(a, fileService.isIndexed(a.getFile().getId())))
                .toList();
    }

    @Transactional
    public void delete(Long assetId, Long attachmentId) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found with id: " + assetId);
        }

        AssetAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));

        if (!attachment.getAsset().getId().equals(assetId)) {
            throw new ResourceNotFoundException("Attachment not found with id: " + attachmentId + " for asset: " + assetId);
        }

        Long fileId = attachment.getFile().getId();
        attachmentRepository.delete(attachment);
        fileService.deleteIfUnused(fileId);
    }

    public FileRecord getAttachmentFile(Long assetId, Long attachmentId) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found with id: " + assetId);
        }
        AssetAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));
        if (!attachment.getAsset().getId().equals(assetId)) {
            throw new ResourceNotFoundException("Attachment not found with id: " + attachmentId + " for asset: " + assetId);
        }
        return attachment.getFile();
    }
}
