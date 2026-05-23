package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateExaminationRequest;
import com.moujitx.homebox.server.dto.response.VisitExaminationResponse;
import com.moujitx.homebox.server.entity.VisitExamination;
import com.moujitx.homebox.server.entity.VisitRecord;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.VisitExaminationRepository;
import com.moujitx.homebox.server.repository.VisitRecordRepository;
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

    @Transactional(readOnly = true)
    public Page<VisitExaminationResponse> list(Long visitId, int page, int size) {
        return repository.findByVisitId(visitId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "examDate")))
                .map(VisitExaminationResponse::from);
    }

    @Transactional(readOnly = true)
    public VisitExaminationResponse getById(Long id) {
        return VisitExaminationResponse.from(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + id)));
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

        return VisitExaminationResponse.from(repository.save(exam));
    }

    @Transactional
    public VisitExaminationResponse update(Long id, CreateExaminationRequest request) {
        VisitExamination exam = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + id));

        if (request.getName() != null) exam.setName(request.getName());
        if (request.getExamDate() != null) exam.setExamDate(request.getExamDate());
        if (request.getDescription() != null) exam.setDescription(request.getDescription());

        return VisitExaminationResponse.from(repository.save(exam));
    }

    @Transactional
    public void delete(Long id) {
        VisitExamination exam = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + id));
        repository.delete(exam);
    }
}
