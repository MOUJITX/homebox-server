package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreatePrescriptionRequest;
import com.moujitx.homebox.server.dto.request.CreatePrescriptionItemRequest;
import com.moujitx.homebox.server.dto.response.PrescriptionItemResponse;
import com.moujitx.homebox.server.dto.response.VisitPrescriptionResponse;
import com.moujitx.homebox.server.entity.*;
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
public class VisitPrescriptionService {

    private final VisitRecordRepository visitRecordRepository;
    private final VisitPrescriptionRepository repository;
    private final PrescriptionItemRepository itemRepository;
    private final MedicationReminderRepository medicationReminderRepository;

    @Transactional(readOnly = true)
    public Page<VisitPrescriptionResponse> list(Long visitId, int page, int size) {
        return repository.findByVisitId(visitId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(VisitPrescriptionResponse::from);
    }

    @Transactional(readOnly = true)
    public VisitPrescriptionResponse getById(Long id) {
        return VisitPrescriptionResponse.from(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + id)));
    }

    @Transactional
    public VisitPrescriptionResponse create(Long visitId, CreatePrescriptionRequest request) {
        VisitRecord visit = visitRecordRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Visit record not found with id: " + visitId));

        VisitPrescription prescription = new VisitPrescription();
        prescription.setVisit(visit);
        prescription.setDescription(request.getDescription());

        return VisitPrescriptionResponse.from(repository.save(prescription));
    }

    @Transactional
    public VisitPrescriptionResponse update(Long id, CreatePrescriptionRequest request) {
        VisitPrescription prescription = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + id));

        if (request.getDescription() != null) prescription.setDescription(request.getDescription());

        return VisitPrescriptionResponse.from(repository.save(prescription));
    }

    @Transactional
    public void delete(Long id) {
        VisitPrescription prescription = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + id));
        repository.delete(prescription);
    }

    // ──────────────────────── Prescription Items ────────────────────────

    @Transactional
    public PrescriptionItemResponse addItem(Long prescriptionId, CreatePrescriptionItemRequest request) {
        VisitPrescription prescription = repository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + prescriptionId));

        MedicationReminder reminder = medicationReminderRepository.findById(request.getMedicationReminderId())
                .orElseThrow(() -> new ResourceNotFoundException("Medication reminder not found with id: " + request.getMedicationReminderId()));

        PrescriptionItem item = new PrescriptionItem();
        item.setPrescription(prescription);
        item.setMedicationReminder(reminder);
        item.setNote(request.getNote());

        return PrescriptionItemResponse.from(itemRepository.save(item));
    }

    @Transactional
    public PrescriptionItemResponse updateItem(Long itemId, CreatePrescriptionItemRequest request) {
        PrescriptionItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription item not found with id: " + itemId));

        if (request.getMedicationReminderId() != null) {
            MedicationReminder reminder = medicationReminderRepository.findById(request.getMedicationReminderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medication reminder not found with id: " + request.getMedicationReminderId()));
            item.setMedicationReminder(reminder);
        }
        if (request.getNote() != null) item.setNote(request.getNote());

        return PrescriptionItemResponse.from(itemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long itemId) {
        PrescriptionItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription item not found with id: " + itemId));
        itemRepository.delete(item);
    }
}
