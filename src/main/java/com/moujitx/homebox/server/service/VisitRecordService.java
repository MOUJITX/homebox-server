package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateVisitRecordRequest;
import com.moujitx.homebox.server.dto.request.UpdateVisitRecordRequest;
import com.moujitx.homebox.server.dto.response.VisitRecordResponse;
import com.moujitx.homebox.server.entity.MedicalInstitution;
import com.moujitx.homebox.server.entity.VisitRecord;
import com.moujitx.homebox.server.enums.VisitType;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitRecordService {

    private final VisitRecordRepository visitRecordRepository;
    private final MedicalInstitutionRepository institutionRepository;
    private final VisitExaminationRepository examinationRepository;
    private final VisitLabTestRepository labTestRepository;
    private final VisitPrescriptionRepository prescriptionRepository;
    private final VisitAttachmentRepository attachmentRepository;
    private final VisitInvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public Page<VisitRecordResponse> list(int page, int size, VisitType visitType, LocalDate startDate,
                                           LocalDate endDate, Long institutionId, String patientName) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "visitDate"));
        String nameFilter = (patientName != null && !patientName.isBlank()) ? patientName : null;
        return visitRecordRepository.findWithFilters(visitType, startDate, endDate, institutionId, nameFilter, pageable)
                .map(VisitRecordResponse::from);
    }

    @Transactional(readOnly = true)
    public VisitRecordResponse getById(Long id) {
        VisitRecord record = visitRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit record not found with id: " + id));
        return VisitRecordResponse.from(record);
    }

    @Transactional(readOnly = true)
    public List<String> getPatientNames() {
        return visitRecordRepository.findDistinctPatientNames();
    }

    @Transactional
    public VisitRecordResponse create(CreateVisitRecordRequest request) {
        MedicalInstitution institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("Medical institution not found with id: " + request.getInstitutionId()));

        VisitRecord record = new VisitRecord();
        record.setPatientName(request.getPatientName());
        record.setPatientAge(request.getPatientAge());
        record.setPatientGender(request.getPatientGender());
        record.setVisitType(request.getVisitType());
        record.setVisitDate(request.getVisitDate());
        record.setInstitution(institution);
        record.setMedicalContent(request.getMedicalContent());
        record.setDoctor(request.getDoctor());
        record.setDepartment(request.getDepartment());
        record.setDischargeDate(request.getDischargeDate());
        record.setDischargeDept(request.getDischargeDept());

        return VisitRecordResponse.from(visitRecordRepository.save(record));
    }

    @Transactional
    public VisitRecordResponse update(Long id, UpdateVisitRecordRequest request) {
        VisitRecord record = visitRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit record not found with id: " + id));

        if (request.getPatientName() != null) record.setPatientName(request.getPatientName());
        if (request.getPatientAge() != null) record.setPatientAge(request.getPatientAge());
        if (request.getPatientGender() != null) record.setPatientGender(request.getPatientGender());
        if (request.getVisitType() != null) record.setVisitType(request.getVisitType());
        if (request.getVisitDate() != null) record.setVisitDate(request.getVisitDate());
        if (request.getInstitutionId() != null) {
            MedicalInstitution institution = institutionRepository.findById(request.getInstitutionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medical institution not found with id: " + request.getInstitutionId()));
            record.setInstitution(institution);
        }
        if (request.getMedicalContent() != null) record.setMedicalContent(request.getMedicalContent());
        if (request.getDoctor() != null) record.setDoctor(request.getDoctor());
        if (request.getDepartment() != null) record.setDepartment(request.getDepartment());
        if (request.getDischargeDate() != null) record.setDischargeDate(request.getDischargeDate());
        if (request.getDischargeDept() != null) record.setDischargeDept(request.getDischargeDept());

        return VisitRecordResponse.from(visitRecordRepository.save(record));
    }

    @Transactional
    public void delete(Long id) {
        VisitRecord record = visitRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit record not found with id: " + id));

        boolean hasSubRecords = examinationRepository.countByVisitId(id) > 0
                || labTestRepository.countByVisitId(id) > 0
                || prescriptionRepository.countByVisitId(id) > 0;

        if (hasSubRecords) {
            throw new OperationNotAllowedException("Cannot delete visit record with existing sub-records");
        }

        visitRecordRepository.delete(record);
    }
}
