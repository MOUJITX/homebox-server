package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.response.VisitInvoiceResponse;
import com.moujitx.homebox.server.entity.Invoice;
import com.moujitx.homebox.server.entity.VisitInvoice;
import com.moujitx.homebox.server.entity.VisitRecord;
import com.moujitx.homebox.server.enums.VisitSourceType;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.InvoiceRepository;
import com.moujitx.homebox.server.repository.VisitInvoiceRepository;
import com.moujitx.homebox.server.repository.VisitRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitInvoiceService {

    private final VisitRecordRepository visitRecordRepository;
    private final VisitInvoiceRepository repository;
    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public List<VisitInvoiceResponse> list(Long visitId) {
        return repository.findByVisitId(visitId).stream()
                .map(VisitInvoiceResponse::from).toList();
    }

    @Transactional
    public VisitInvoiceResponse bind(Long visitId, Long invoiceId, VisitSourceType sourceType, Long sourceId) {
        VisitRecord visit = visitRecordRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Visit record not found with id: " + visitId));

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        VisitInvoice vi = new VisitInvoice();
        vi.setVisit(visit);
        vi.setInvoice(invoice);
        vi.setSourceType(sourceType);
        vi.setSourceId(sourceId);

        return VisitInvoiceResponse.from(repository.save(vi));
    }

    @Transactional
    public void unbind(Long id) {
        VisitInvoice vi = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit invoice binding not found with id: " + id));
        repository.delete(vi);
    }
}
