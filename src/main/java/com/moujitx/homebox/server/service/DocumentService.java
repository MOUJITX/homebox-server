package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateDocumentRequest;
import com.moujitx.homebox.server.dto.request.UpdateDocumentRequest;
import com.moujitx.homebox.server.dto.response.DocumentAttachmentResponse;
import com.moujitx.homebox.server.dto.response.DocumentDetailResponse;
import com.moujitx.homebox.server.dto.response.DocumentInvoiceResponse;
import com.moujitx.homebox.server.dto.response.DocumentResponse;
import com.moujitx.homebox.server.entity.*;
import com.moujitx.homebox.server.enums.DocumentStatus;
import com.moujitx.homebox.server.enums.Importance;
import com.moujitx.homebox.server.enums.NotificationType;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.*;
import com.moujitx.homebox.server.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentCategoryRepository categoryRepository;
    private final DocumentAttachmentRepository attachmentRepository;
    private final DocumentInvoiceRepository documentInvoiceRepository;
    private final InvoiceRepository invoiceRepository;
    private final NotificationRepository notificationRepository;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public Page<DocumentResponse> getDocuments(String search, Long categoryId, String status,
                                                String importance, Boolean parentOnly, Pageable pageable) {
        String searchParam = StringUtil.normalizeSearch(search);

        Page<Document> documentsPage = documentRepository.findWithFilters(
                searchParam, categoryId, status, importance, parentOnly, pageable);

        List<Document> documents = documentsPage.getContent();
        Map<Long, Integer> subDocumentCounts = loadSubDocumentCounts(documents);
        Map<Long, Boolean> invoiceFlags = loadInvoiceFlags(documents);

        List<DocumentResponse> responses = documents.stream()
                .map(doc -> {
                    DocumentResponse r = DocumentResponse.from(doc, subDocumentCounts);
                    r.setHasInvoice(invoiceFlags.getOrDefault(doc.getId(), false));
                    return r;
                })
                .toList();
        return new PageImpl<>(responses, pageable, documentsPage.getTotalElements());
    }

    private Map<Long, Integer> loadSubDocumentCounts(List<Document> documents) {
        List<Long> ids = documents.stream().map(Document::getId).toList();
        if (ids.isEmpty()) return Map.of();
        return documentRepository.countSubDocumentsGroupedByParent(ids).stream()
                .collect(Collectors.toMap(t -> (Long) t.get(0), t -> ((Number) t.get(1)).intValue(), (a, b) -> b));
    }

    private Map<Long, Boolean> loadInvoiceFlags(List<Document> documents) {
        List<Long> ids = documents.stream().map(Document::getId).toList();
        if (ids.isEmpty()) return Map.of();
        return documentInvoiceRepository.findByInvoiceIdIn(ids).stream()
                .collect(Collectors.toMap(di -> di.getDocument().getId(), di -> true, (a, b) -> b));
    }

    @Transactional(readOnly = true)
    public DocumentDetailResponse getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        List<Document> subDocEntities = documentRepository.findByParentIdOrderByCreatedAtDesc(id);
        List<DocumentResponse> subDocuments = subDocEntities.stream()
                .map(sub -> DocumentResponse.from(sub, 0))
                .toList();

        List<DocumentInvoiceResponse> invoices = documentInvoiceRepository.findByDocumentId(id).stream()
                .map(DocumentInvoiceResponse::from)
                .toList();

        List<DocumentAttachmentResponse> attachments = attachmentRepository.findByDocumentId(id).stream()
                .map(a -> DocumentAttachmentResponse.from(a, fileService.isIndexed(a.getFile().getId())))
                .toList();

        DocumentDetailResponse detail = DocumentDetailResponse.from(
                document, subDocuments.size(), subDocuments, invoices);
        detail.setAttachments(attachments);
        return detail;
    }

    @Transactional
    public DocumentDetailResponse createDocument(CreateDocumentRequest request) {
        DocumentCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Document document = new Document();
        document.setName(request.getName());
        document.setCategory(category);

        if (request.getParentId() != null) {
            Document parent = documentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent document not found with id: " + request.getParentId()));
            if (parent.getParent() != null) {
                throw new OperationNotAllowedException("Cannot create sub-document under another sub-document");
            }
            document.setParent(parent);
        }

        document.setHolder(request.getHolder());
        document.setDocumentNumber(request.getDocumentNumber());
        document.setIssuer(request.getIssuer());
        document.setIssueDate(request.getIssueDate());
        document.setExpiryDate(request.getExpiryDate());
        document.setStatus(request.getStatus() != null ? request.getStatus() : DocumentStatus.ACTIVE);
        document.setImportance(request.getImportance() != null ? request.getImportance() : Importance.MEDIUM);
        document.setReminderDays(request.getReminderDays() != null ? request.getReminderDays() : 7);
        document.setNote(request.getNote());

        Document saved = documentRepository.save(document);
        return DocumentDetailResponse.from(saved, 0, List.of(), List.of());
    }

    @Transactional
    public DocumentDetailResponse updateDocument(Long id, UpdateDocumentRequest request) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        if (request.getName() != null) {
            document.setName(request.getName());
        }
        if (request.getCategoryId() != null) {
            DocumentCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            document.setCategory(category);
        }
        if (request.getHolder() != null) {
            document.setHolder(request.getHolder().isEmpty() ? null : request.getHolder());
        }
        if (request.getDocumentNumber() != null) {
            document.setDocumentNumber(request.getDocumentNumber().isEmpty() ? null : request.getDocumentNumber());
        }
        if (request.getIssuer() != null) {
            document.setIssuer(request.getIssuer().isEmpty() ? null : request.getIssuer());
        }
        if (request.getIssueDate() != null) {
            document.setIssueDate(request.getIssueDate());
        }
        if (request.getExpiryDate() != null) {
            document.setExpiryDate(request.getExpiryDate());
        }
        if (request.getStatus() != null) {
            document.setStatus(request.getStatus());
        }
        if (request.getImportance() != null) {
            document.setImportance(request.getImportance());
        }
        if (request.getReminderDays() != null) {
            document.setReminderDays(request.getReminderDays());
        }
        if (request.getNote() != null) {
            document.setNote(request.getNote().isEmpty() ? null : request.getNote());
        }

        Document saved = documentRepository.save(document);
        List<Document> subDocEntities = documentRepository.findByParentIdOrderByCreatedAtDesc(id);
        List<DocumentResponse> subDocuments = subDocEntities.stream()
                .map(sub -> DocumentResponse.from(sub, 0))
                .toList();
        List<DocumentInvoiceResponse> invoices = documentInvoiceRepository.findByDocumentId(id).stream()
                .map(DocumentInvoiceResponse::from)
                .toList();

        return DocumentDetailResponse.from(saved, subDocuments.size(), subDocuments, invoices);
    }

    @Transactional
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        if (documentRepository.existsByParentId(id)) {
            throw new OperationNotAllowedException("Cannot delete document that has sub-documents. Delete all sub-documents first.");
        }

        for (var attachment : document.getAttachments()) {
            fileService.delete(attachment.getFile().getId());
        }

        documentRepository.delete(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentInvoiceResponse> getDocumentInvoices(Long documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found with id: " + documentId);
        }
        return documentInvoiceRepository.findByDocumentId(documentId).stream()
                .map(DocumentInvoiceResponse::from)
                .toList();
    }

    @Transactional
    public void bindInvoice(Long documentId, Long invoiceId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        if (documentInvoiceRepository.existsByDocumentIdAndInvoiceId(documentId, invoiceId)) {
            throw new ResourceAlreadyExistsException("Invoice already bound to this document");
        }

        DocumentInvoice binding = new DocumentInvoice();
        binding.setDocument(document);
        binding.setInvoice(invoice);
        documentInvoiceRepository.save(binding);
    }

    @Transactional
    public void unbindInvoice(Long documentId, Long invoiceId) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found with id: " + documentId);
        }
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + invoiceId);
        }
        documentInvoiceRepository.deleteByDocumentIdAndInvoiceId(documentId, invoiceId);
    }

    @Transactional
    public void checkAndNotify() {
        log.info("Running document expiry check");
        LocalDate today = LocalDate.now();

        List<Document> allDocs = documentRepository.findAll();
        List<Notification> newNotifications = new ArrayList<>();

        for (Document doc : allDocs) {
            if (doc.getExpiryDate() == null || doc.getStatus() != DocumentStatus.ACTIVE) {
                continue;
            }

            int noticeDays = doc.getReminderDays() != null ? doc.getReminderDays() : 7;
            LocalDate deadline = today.plusDays(noticeDays);

            if (!doc.getExpiryDate().isBefore(today) && !doc.getExpiryDate().isAfter(deadline)) {
                String title = "文档到期提醒";
                String content = "您的文档「" + doc.getName() + "」将于 " + doc.getExpiryDate() + " 到期，请及时处理";

                Notification notification = new Notification();
                notification.setType(NotificationType.DOCUMENT_EXPIRY);
                notification.setTitle(title);
                notification.setContent(content);
                notification.setSourceType("DOCUMENT");
                notification.setSourceId(doc.getId());
                notification.setNotifyDate(today);
                newNotifications.add(notification);
            }
        }

        if (!newNotifications.isEmpty()) {
            notificationRepository.saveAll(newNotifications);
            log.info("Created {} document expiry notifications", newNotifications.size());
        }

        documentRepository.findAll().stream()
                .filter(d -> d.getExpiryDate() != null && d.getStatus() == DocumentStatus.ACTIVE && d.getExpiryDate().isBefore(today))
                .forEach(d -> {
                    d.setStatus(DocumentStatus.EXPIRED);
                    documentRepository.save(d);
                    log.info("Auto-expired document: {}", d.getName());
                });
    }
}
