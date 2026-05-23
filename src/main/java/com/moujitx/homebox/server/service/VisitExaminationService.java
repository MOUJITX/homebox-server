package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateExaminationRequest;
import com.moujitx.homebox.server.dto.response.VisitExaminationResponse;
import com.moujitx.homebox.server.entity.VisitExamination;
import com.moujitx.homebox.server.entity.VisitRecord;
import com.moujitx.homebox.server.enums.VisitSourceType;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VisitExaminationService {

    private final VisitRecordRepository visitRecordRepository;
    private final VisitExaminationRepository repository;
    private final VisitAttachmentRepository attachmentRepository;
    private final VisitInvoiceRepository invoiceRepository;
    private final FileRecordRepository fileRecordRepository;

    @Transactional(readOnly = true)
    public Page<VisitExaminationResponse> list(Long visitId, int page, int size) {
        return repository.findByVisitId(visitId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "examDate")))
                .map(e -> VisitExaminationResponse.from(e,
                        attachmentRepository.findByVisitIdAndSourceType(e.getVisit().getId(), VisitSourceType.EXAMINATION).size(),
                        invoiceRepository.findByVisitIdAndSourceType(e.getVisit().getId(), VisitSourceType.EXAMINATION).size()));
    }

    @Transactional(readOnly = true)
    public VisitExaminationResponse getById(Long id) {
        VisitExamination e = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + id));
        long attachCount = attachmentRepository.findByVisitIdAndSourceType(e.getVisit().getId(), VisitSourceType.EXAMINATION).size();
        long invCount = invoiceRepository.findByVisitIdAndSourceType(e.getVisit().getId(), VisitSourceType.EXAMINATION).size();
        return VisitExaminationResponse.from(e, attachCount, invCount);
    }

    @Transactional
    public VisitExaminationResponse create(Long visitId, CreateExaminationRequest request) {
        VisitRecord visit = visitRecordRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Visit record not found with id: " + visitId));

        VisitExamination exam = new VisitExamination();
        exam.setVisit(visit);
        exam.setName(request.getName());
        exam.setExamDate(request.getExamDate());
        exam.setDescription(request.getDescription());

        return VisitExaminationResponse.from(repository.save(exam), 0, 0);
    }

    @Transactional
    public VisitExaminationResponse update(Long id, CreateExaminationRequest request) {
        VisitExamination exam = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + id));

        if (request.getName() != null) exam.setName(request.getName());
        if (request.getExamDate() != null) exam.setExamDate(request.getExamDate());
        if (request.getDescription() != null) exam.setDescription(request.getDescription());

        long attachCount = attachmentRepository.findByVisitIdAndSourceType(exam.getVisit().getId(), VisitSourceType.EXAMINATION).size();
        long invCount = invoiceRepository.findByVisitIdAndSourceType(exam.getVisit().getId(), VisitSourceType.EXAMINATION).size();
        return VisitExaminationResponse.from(repository.save(exam), attachCount, invCount);
    }

    @Transactional
    public void delete(Long id) {
        VisitExamination exam = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + id));

        Long visitId = exam.getVisit().getId();

        // Unbind invoices (don't delete invoices themselves)
        invoiceRepository.deleteByVisitIdAndSourceTypeAndSourceId(visitId, VisitSourceType.EXAMINATION, id);

        // Cascade delete attachments (only if file not referenced elsewhere)
        var attachments = attachmentRepository.findByVisitIdAndSourceType(visitId, VisitSourceType.EXAMINATION);
        for (var a : attachments) {
            if (a.getSourceId().equals(id)) {
                Long fileId = a.getFile().getId();
                attachmentRepository.delete(a);
                if (attachmentRepository.findByFileId(fileId).isEmpty()) {
                    fileRecordRepository.deleteById(fileId);
                }
            }
        }

        repository.delete(exam);
    }
}
