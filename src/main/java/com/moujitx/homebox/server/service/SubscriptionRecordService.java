package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.SubscriptionRecordRequest;
import com.moujitx.homebox.server.dto.response.SubscriptionRecordAttachmentResponse;
import com.moujitx.homebox.server.dto.response.SubscriptionRecordInvoiceResponse;
import com.moujitx.homebox.server.dto.response.SubscriptionRecordResponse;
import com.moujitx.homebox.server.entity.*;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionRecordService {

    private final SubscriptionRecordRepository recordRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionRecordAttachmentRepository attachmentRepository;
    private final SubscriptionRecordInvoiceRepository recordInvoiceRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final InvoiceRepository invoiceRepository;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public List<SubscriptionRecordResponse> getRecords(Long subscriptionId) {
        if (!subscriptionRepository.existsById(subscriptionId)) {
            throw new ResourceNotFoundException("Subscription not found with id: " + subscriptionId);
        }

        return recordRepository.findBySubscriptionIdOrderByStartDateDesc(subscriptionId).stream()
                .map(SubscriptionRecordResponse::from)
                .toList();
    }

    @Transactional
    public SubscriptionRecordResponse addRecord(Long subscriptionId, SubscriptionRecordRequest request) {
        if (request.getRecordDate() == null || request.getAmount() == null || request.getStartDate() == null) {
            throw new IllegalArgumentException("recordDate, amount, and startDate are required");
        }

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));

        SubscriptionRecord record = new SubscriptionRecord();
        record.setSubscription(subscription);
        record.setRecordDate(request.getRecordDate());
        record.setAmount(request.getAmount());
        record.setCurrency(request.getCurrency() != null ? request.getCurrency() : "CNY");
        record.setStartDate(request.getStartDate());
        record.setEndDate(request.getEndDate());
        record.setQuantity(request.getQuantity());
        record.setOrderNo(request.getOrderNo() != null && !request.getOrderNo().isBlank() ? request.getOrderNo() : null);
        record.setNote(request.getNote());
        if (request.getExpired() != null) record.setExpired(request.getExpired());

        if (record.getOrderNo() != null && recordRepository.existsByOrderNo(record.getOrderNo())) {
            throw new ResourceAlreadyExistsException("Order number already exists: " + record.getOrderNo());
        }

        if (request.getPaymentMethodId() != null) {
            PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment method not found with id: " + request.getPaymentMethodId()));
            record.setPaymentMethod(paymentMethod);
        }

        SubscriptionRecord saved = recordRepository.save(record);
        return SubscriptionRecordResponse.from(saved);
    }

    @Transactional
    public SubscriptionRecordResponse updateRecord(Long subscriptionId, Long recordId, SubscriptionRecordRequest request) {
        if (!subscriptionRepository.existsById(subscriptionId)) {
            throw new ResourceNotFoundException("Subscription not found with id: " + subscriptionId);
        }

        SubscriptionRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + recordId));

        if (!record.getSubscription().getId().equals(subscriptionId)) {
            throw new ResourceNotFoundException("Record not found for this subscription");
        }

        if (request.getRecordDate() != null) record.setRecordDate(request.getRecordDate());
        if (request.getAmount() != null) record.setAmount(request.getAmount());
        if (request.getCurrency() != null) record.setCurrency(request.getCurrency());
        if (request.getStartDate() != null) record.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) record.setEndDate(request.getEndDate());
        if (request.getQuantity() != null) record.setQuantity(request.getQuantity().isEmpty() ? null : request.getQuantity());
        if (request.getOrderNo() != null) {
            String orderNo = request.getOrderNo().isBlank() ? null : request.getOrderNo();
            if (orderNo != null && recordRepository.existsByOrderNoAndIdNot(orderNo, recordId)) {
                throw new ResourceAlreadyExistsException("Order number already exists: " + orderNo);
            }
            record.setOrderNo(orderNo);
        }
        if (request.getExpired() != null) record.setExpired(request.getExpired());
        if (request.getNote() != null) record.setNote(request.getNote().isEmpty() ? null : request.getNote());

        if (request.getPaymentMethodId() != null) {
            PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment method not found with id: " + request.getPaymentMethodId()));
            record.setPaymentMethod(paymentMethod);
        }

        SubscriptionRecord saved = recordRepository.save(record);
        return SubscriptionRecordResponse.from(saved);
    }

    @Transactional
    public void deleteRecord(Long subscriptionId, Long recordId) {
        if (!subscriptionRepository.existsById(subscriptionId)) {
            throw new ResourceNotFoundException("Subscription not found with id: " + subscriptionId);
        }

        SubscriptionRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + recordId));

        if (!record.getSubscription().getId().equals(subscriptionId)) {
            throw new ResourceNotFoundException("Record not found for this subscription");
        }

        List<Long> fileIds = record.getAttachments().stream()
                .map(a -> a.getFile().getId())
                .toList();

        recordRepository.delete(record);

        for (Long fileId : fileIds) {
            fileService.deleteIfUnused(fileId);
        }
    }

    // ── Attachments ──

    @Transactional(readOnly = true)
    public List<SubscriptionRecordAttachmentResponse> getAttachments(Long recordId) {
        return attachmentRepository.findByRecordId(recordId).stream()
                .map(SubscriptionRecordAttachmentResponse::from)
                .toList();
    }

    @Transactional
    public SubscriptionRecordAttachmentResponse uploadAttachment(Long recordId, MultipartFile file) {
        SubscriptionRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + recordId));

        FileRecord fileRecord = fileService.upload(file);

        SubscriptionRecordAttachment attachment = new SubscriptionRecordAttachment();
        attachment.setRecord(record);
        attachment.setFile(fileRecord);
        attachmentRepository.save(attachment);

        return SubscriptionRecordAttachmentResponse.from(attachment);
    }

    @Transactional
    public SubscriptionRecordAttachmentResponse linkAttachment(Long recordId, Long fileId) {
        SubscriptionRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + recordId));

        FileRecord fileRecord = fileService.getFileById(fileId);

        SubscriptionRecordAttachment attachment = new SubscriptionRecordAttachment();
        attachment.setRecord(record);
        attachment.setFile(fileRecord);
        attachmentRepository.save(attachment);

        return SubscriptionRecordAttachmentResponse.from(attachment);
    }

    @Transactional
    public List<SubscriptionRecordAttachmentResponse> sync(Long recordId, List<Long> fileIds) {
        SubscriptionRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + recordId));

        List<Long> desired = fileIds == null ? List.of() : new ArrayList<>(new LinkedHashSet<>(fileIds));
        List<SubscriptionRecordAttachment> existing = attachmentRepository.findByRecordId(recordId);

        // remove unlisted
        for (SubscriptionRecordAttachment a : existing) {
            if (!desired.contains(a.getFile().getId())) {
                Long fileId = a.getFile().getId();
                attachmentRepository.delete(a);
                fileService.deleteIfUnused(fileId);
            }
        }

        // link missing
        Set<Long> existingFileIds = existing.stream()
                .map(a -> a.getFile().getId())
                .collect(Collectors.toSet());

        for (Long fileId : desired) {
            if (existingFileIds.contains(fileId)) continue;
            FileRecord fileRecord = fileService.getFileById(fileId);
            SubscriptionRecordAttachment attachment = new SubscriptionRecordAttachment();
            attachment.setRecord(record);
            attachment.setFile(fileRecord);
            attachmentRepository.save(attachment);
        }

        return attachmentRepository.findByRecordId(recordId).stream()
                .map(SubscriptionRecordAttachmentResponse::from)
                .toList();
    }

    @Transactional
    public void deleteAttachment(Long recordId, Long attachmentId) {
        SubscriptionRecordAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));

        if (!attachment.getRecord().getId().equals(recordId)) {
            throw new ResourceNotFoundException("Attachment not found for this record");
        }

        Long fileId = attachment.getFile().getId();
        attachmentRepository.delete(attachment);
        fileService.deleteIfUnused(fileId);
    }

    // ── Invoice Bindings ──

    @Transactional(readOnly = true)
    public List<SubscriptionRecordInvoiceResponse> getInvoices(Long recordId) {
        return recordInvoiceRepository.findByRecordId(recordId).stream()
                .map(SubscriptionRecordInvoiceResponse::from)
                .toList();
    }

    @Transactional
    public void bindInvoice(Long recordId, Long invoiceId) {
        SubscriptionRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + recordId));

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        if (recordInvoiceRepository.existsByRecordIdAndInvoiceId(recordId, invoiceId)) {
            throw new ResourceAlreadyExistsException("Invoice already bound to this record");
        }

        SubscriptionRecordInvoice binding = new SubscriptionRecordInvoice();
        binding.setRecord(record);
        binding.setInvoice(invoice);
        recordInvoiceRepository.save(binding);
    }

    @Transactional
    public void unbindInvoice(Long recordId, Long invoiceId) {
        if (!recordInvoiceRepository.existsByRecordIdAndInvoiceId(recordId, invoiceId)) {
            throw new ResourceNotFoundException("Invoice binding not found");
        }

        recordInvoiceRepository.deleteByRecordIdAndInvoiceId(recordId, invoiceId);
    }
}
