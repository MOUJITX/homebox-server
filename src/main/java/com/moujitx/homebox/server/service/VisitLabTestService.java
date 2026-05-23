package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateLabTestRequest;
import com.moujitx.homebox.server.dto.response.VisitLabTestResponse;
import com.moujitx.homebox.server.entity.VisitLabTest;
import com.moujitx.homebox.server.entity.VisitRecord;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.VisitLabTestRepository;
import com.moujitx.homebox.server.repository.VisitRecordRepository;
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

    @Transactional(readOnly = true)
    public Page<VisitLabTestResponse> list(Long visitId, int page, int size) {
        return repository.findByVisitId(visitId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "testDate")))
                .map(VisitLabTestResponse::from);
    }

    @Transactional(readOnly = true)
    public VisitLabTestResponse getById(Long id) {
        return VisitLabTestResponse.from(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with id: " + id)));
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

        return VisitLabTestResponse.from(repository.save(test));
    }

    @Transactional
    public VisitLabTestResponse update(Long id, CreateLabTestRequest request) {
        VisitLabTest test = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with id: " + id));

        if (request.getName() != null) test.setName(request.getName());
        if (request.getTestDate() != null) test.setTestDate(request.getTestDate());
        if (request.getDescription() != null) test.setDescription(request.getDescription());

        return VisitLabTestResponse.from(repository.save(test));
    }

    @Transactional
    public void delete(Long id) {
        VisitLabTest test = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with id: " + id));
        repository.delete(test);
    }
}
