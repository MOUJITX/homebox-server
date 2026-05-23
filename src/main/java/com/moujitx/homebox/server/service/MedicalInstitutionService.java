package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.MedicalInstitutionRequest;
import com.moujitx.homebox.server.dto.response.MedicalInstitutionResponse;
import com.moujitx.homebox.server.entity.MedicalInstitution;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.MedicalInstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalInstitutionService {

    private final MedicalInstitutionRepository repository;

    @Transactional(readOnly = true)
    public List<MedicalInstitutionResponse> list() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream().map(MedicalInstitutionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Page<MedicalInstitutionResponse> page(int page, int size, String name) {
        Page<MedicalInstitution> result;
        if (name != null && !name.isBlank()) {
            result = repository.findByNameContainingIgnoreCase(name,
                    PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
        } else {
            result = repository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
        }
        return result.map(MedicalInstitutionResponse::from);
    }

    @Transactional(readOnly = true)
    public MedicalInstitutionResponse getById(Long id) {
        return MedicalInstitutionResponse.from(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical institution not found with id: " + id)));
    }

    @Transactional
    public MedicalInstitutionResponse create(MedicalInstitutionRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Medical institution already exists with name: " + request.getName());
        }
        MedicalInstitution institution = new MedicalInstitution();
        institution.setName(request.getName());
        institution.setNote(request.getNote());
        return MedicalInstitutionResponse.from(repository.save(institution));
    }

    @Transactional
    public MedicalInstitutionResponse update(Long id, MedicalInstitutionRequest request) {
        MedicalInstitution institution = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical institution not found with id: " + id));
        institution.setName(request.getName());
        institution.setNote(request.getNote());
        return MedicalInstitutionResponse.from(repository.save(institution));
    }

    @Transactional
    public void delete(Long id) {
        MedicalInstitution institution = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical institution not found with id: " + id));
        repository.delete(institution);
    }
}
