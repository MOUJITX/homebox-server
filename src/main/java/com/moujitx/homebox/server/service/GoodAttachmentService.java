package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.response.GoodAttachmentResponse;
import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.entity.Good;
import com.moujitx.homebox.server.entity.GoodAttachment;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.GoodAttachmentRepository;
import com.moujitx.homebox.server.repository.GoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodAttachmentService {

    private final GoodAttachmentRepository attachmentRepository;
    private final GoodRepository goodRepository;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public List<GoodAttachmentResponse> getByGoodId(Long goodId) {
        if (!goodRepository.existsById(goodId)) {
            throw new ResourceNotFoundException("Good not found with id: " + goodId);
        }
        return attachmentRepository.findByGoodId(goodId).stream()
                .map(a -> GoodAttachmentResponse.from(a, fileService.isIndexed(a.getFile().getId())))
                .toList();
    }

    @Transactional
    public GoodAttachmentResponse upload(Long goodId, MultipartFile file) {
        Good good = goodRepository.findById(goodId)
                .orElseThrow(() -> new ResourceNotFoundException("Good not found with id: " + goodId));

        FileRecord fileRecord = fileService.upload(file);

        GoodAttachment attachment = new GoodAttachment();
        attachment.setGood(good);
        attachment.setFile(fileRecord);

        attachment = attachmentRepository.save(attachment);
        return GoodAttachmentResponse.from(attachment, false);
    }

    @Transactional
    public GoodAttachmentResponse link(Long goodId, Long fileId) {
        Good good = goodRepository.findById(goodId)
                .orElseThrow(() -> new ResourceNotFoundException("Good not found with id: " + goodId));

        FileRecord fileRecord = fileService.getFileById(fileId);

        GoodAttachment attachment = new GoodAttachment();
        attachment.setGood(good);
        attachment.setFile(fileRecord);

        return GoodAttachmentResponse.from(attachmentRepository.save(attachment), fileService.isIndexed(fileId));
    }

    @Transactional
    public void delete(Long goodId, Long attachmentId) {
        if (!goodRepository.existsById(goodId)) {
            throw new ResourceNotFoundException("Good not found with id: " + goodId);
        }

        GoodAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));

        if (!attachment.getGood().getId().equals(goodId)) {
            throw new ResourceNotFoundException("Attachment not found with id: " + attachmentId + " for good: " + goodId);
        }

        Long fileId = attachment.getFile().getId();
        attachmentRepository.delete(attachment);
        fileService.delete(fileId);
    }

    public FileRecord getAttachmentFile(Long goodId, Long attachmentId) {
        if (!goodRepository.existsById(goodId)) {
            throw new ResourceNotFoundException("Good not found with id: " + goodId);
        }
        GoodAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));
        if (!attachment.getGood().getId().equals(goodId)) {
            throw new ResourceNotFoundException("Attachment not found with id: " + attachmentId + " for good: " + goodId);
        }
        return attachment.getFile();
    }
}
