package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.response.DocumentAttachmentResponse;
import com.moujitx.homebox.server.entity.Document;
import com.moujitx.homebox.server.entity.DocumentAttachment;
import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.DocumentAttachmentRepository;
import com.moujitx.homebox.server.repository.DocumentRepository;
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
public class DocumentAttachmentService {

    private final DocumentAttachmentRepository attachmentRepository;
    private final DocumentRepository documentRepository;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public List<DocumentAttachmentResponse> getByDocumentId(Long documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found with id: " + documentId);
        }
        return attachmentRepository.findByDocumentId(documentId).stream()
                .map(a -> DocumentAttachmentResponse.from(a, fileService.isIndexed(a.getFile().getId())))
                .toList();
    }

    @Transactional
    public DocumentAttachmentResponse upload(Long documentId, MultipartFile file) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        FileRecord fileRecord = fileService.upload(file);

        DocumentAttachment attachment = new DocumentAttachment();
        attachment.setDocument(document);
        attachment.setFile(fileRecord);

        attachment = attachmentRepository.save(attachment);
        return DocumentAttachmentResponse.from(attachment, false);
    }

    @Transactional
    public DocumentAttachmentResponse link(Long documentId, Long fileId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        FileRecord fileRecord = fileService.getFileById(fileId);

        DocumentAttachment attachment = new DocumentAttachment();
        attachment.setDocument(document);
        attachment.setFile(fileRecord);

        return DocumentAttachmentResponse.from(attachmentRepository.save(attachment), fileService.isIndexed(fileId));
    }

    @Transactional
    public List<DocumentAttachmentResponse> sync(Long documentId, List<Long> fileIds) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        List<Long> desired = fileIds == null ? List.of() : new ArrayList<>(new LinkedHashSet<>(fileIds));
        List<DocumentAttachment> existing = attachmentRepository.findByDocumentId(documentId);

        // remove unlisted
        for (DocumentAttachment a : existing) {
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
            DocumentAttachment attachment = new DocumentAttachment();
            attachment.setDocument(document);
            attachment.setFile(fileRecord);
            attachmentRepository.save(attachment);
        }

        return attachmentRepository.findByDocumentId(documentId).stream()
                .map(a -> DocumentAttachmentResponse.from(a, fileService.isIndexed(a.getFile().getId())))
                .toList();
    }

    @Transactional
    public void delete(Long documentId, Long attachmentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found with id: " + documentId);
        }

        DocumentAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));

        if (!attachment.getDocument().getId().equals(documentId)) {
            throw new ResourceNotFoundException("Attachment not found with id: " + attachmentId + " for document: " + documentId);
        }

        Long fileId = attachment.getFile().getId();
        attachmentRepository.delete(attachment);
        fileService.deleteIfUnused(fileId);
    }

    public FileRecord getAttachmentFile(Long documentId, Long attachmentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found with id: " + documentId);
        }
        DocumentAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));
        if (!attachment.getDocument().getId().equals(documentId)) {
            throw new ResourceNotFoundException("Attachment not found with id: " + attachmentId + " for document: " + documentId);
        }
        return attachment.getFile();
    }
}
