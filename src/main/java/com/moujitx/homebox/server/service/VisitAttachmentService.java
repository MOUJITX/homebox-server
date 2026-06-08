package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.response.VisitAttachmentResponse;
import com.moujitx.homebox.server.entity.*;
import com.moujitx.homebox.server.enums.VisitSourceType;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.VisitAttachmentRepository;
import com.moujitx.homebox.server.repository.VisitRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitAttachmentService {

    private final VisitRecordRepository visitRecordRepository;
    private final VisitAttachmentRepository repository;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public List<VisitAttachmentResponse> list(Long visitId) {
        return repository.findByVisitId(visitId).stream()
                .map(VisitAttachmentResponse::from).toList();
    }

    @Transactional
    public VisitAttachmentResponse upload(Long visitId, MultipartFile file, VisitSourceType sourceType, Long sourceId) {
        VisitRecord visit = visitRecordRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Visit record not found with id: " + visitId));

        FileRecord fileRecord = fileService.upload(file);

        VisitAttachment attachment = new VisitAttachment();
        attachment.setVisit(visit);
        attachment.setFile(fileRecord);
        attachment.setSourceType(sourceType);
        attachment.setSourceId(sourceId);

        return VisitAttachmentResponse.from(repository.save(attachment));
    }

    @Transactional
    public VisitAttachmentResponse link(Long visitId, Long fileId, VisitSourceType sourceType, Long sourceId) {
        VisitRecord visit = visitRecordRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Visit record not found with id: " + visitId));

        FileRecord fileRecord = fileService.getFileById(fileId);

        VisitAttachment attachment = new VisitAttachment();
        attachment.setVisit(visit);
        attachment.setFile(fileRecord);
        attachment.setSourceType(sourceType);
        attachment.setSourceId(sourceId);

        return VisitAttachmentResponse.from(repository.save(attachment));
    }

    @Transactional
    public void delete(Long id) {
        VisitAttachment attachment = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit attachment not found with id: " + id));

        Long fileId = attachment.getFile().getId();
        repository.delete(attachment);
        fileService.deleteIfUnused(fileId);
    }
}
