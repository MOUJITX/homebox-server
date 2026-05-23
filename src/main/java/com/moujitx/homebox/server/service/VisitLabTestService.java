package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateLabTestRequest;
import com.moujitx.homebox.server.dto.response.VisitLabTestResponse;
import com.moujitx.homebox.server.entity.VisitLabTest;
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
public class VisitLabTestService {

    private final VisitRecordRepository visitRecordRepository;
    private final VisitLabTestRepository repository;
    private final VisitAttachmentRepository attachmentRepository;
    private final VisitInvoiceRepository invoiceRepository;
    private final FileRecordRepository fileRecordRepository;

    @Transactional(readOnly = true)
    public Page<VisitLabTestResponse> list(Long visitId, int page, int size) {
        return repository.findByVisitId(visitId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "testDate")))
                .map(t -> VisitLabTestResponse.from(t,
                        attachmentRepository.findByVisitIdAndSourceType(t.getVisit().getId(), VisitSourceType.LAB_TEST).size(),
                        invoiceRepository.findByVisitIdAndSourceType(t.getVisit().getId(), VisitSourceType.LAB_TEST).size()));
    }

    @Transactional(readOnly = true)
    public VisitLabTestResponse getById(Long id) {
        VisitLabTest t = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with id: " + id));
        long attachCount = attachmentRepository.findByVisitIdAndSourceType(t.getVisit().getId(), VisitSourceType.LAB_TEST).size();
        long invCount = invoiceRepository.findByVisitIdAndSourceType(t.getVisit().getId(), VisitSourceType.LAB_TEST).size();
        return VisitLabTestResponse.from(t, attachCount, invCount);
    }

    @Transactional
    public VisitLabTestResponse create(Long visitId, CreateLabTestRequest request) {
        VisitRecord visit = visitRecordRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Visit record not found with id: " + visitId));

        VisitLabTest test = new VisitLabTest();
        test.setVisit(visit);
        test.setName(request.getName());
        test.setTestDate(request.getTestDate());
        test.setDescription(request.getDescription());

        return VisitLabTestResponse.from(repository.save(test), 0, 0);
    }

    @Transactional
    public VisitLabTestResponse update(Long id, CreateLabTestRequest request) {
        VisitLabTest test = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with id: " + id));

        if (request.getName() != null) test.setName(request.getName());
        if (request.getTestDate() != null) test.setTestDate(request.getTestDate());
        if (request.getDescription() != null) test.setDescription(request.getDescription());

        long attachCount = attachmentRepository.findByVisitIdAndSourceType(test.getVisit().getId(), VisitSourceType.LAB_TEST).size();
        long invCount = invoiceRepository.findByVisitIdAndSourceType(test.getVisit().getId(), VisitSourceType.LAB_TEST).size();
        return VisitLabTestResponse.from(repository.save(test), attachCount, invCount);
    }

    @Transactional
    public void delete(Long id) {
        VisitLabTest test = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with id: " + id));

        Long visitId = test.getVisit().getId();
        invoiceRepository.deleteByVisitIdAndSourceTypeAndSourceId(visitId, VisitSourceType.LAB_TEST, id);

        var attachments = attachmentRepository.findByVisitIdAndSourceType(visitId, VisitSourceType.LAB_TEST);
        for (var a : attachments) {
            if (a.getSourceId().equals(id)) {
                Long fileId = a.getFile().getId();
                attachmentRepository.delete(a);
                if (attachmentRepository.findByFileId(fileId).isEmpty()) {
                    fileRecordRepository.deleteById(fileId);
                }
            }
        }

        repository.delete(test);
    }
}
