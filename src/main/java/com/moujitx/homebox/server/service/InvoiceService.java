package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateInvoiceRequest;
import com.moujitx.homebox.server.dto.request.UpdateInvoiceRequest;
import com.moujitx.homebox.server.dto.response.InvoiceAttachmentResponse;
import com.moujitx.homebox.server.dto.response.InvoiceDetailResponse;
import com.moujitx.homebox.server.dto.response.InvoiceParseResponse;
import com.moujitx.homebox.server.dto.response.InvoiceResponse;
import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.entity.Invoice;
import com.moujitx.homebox.server.entity.InvoiceAttachment;
import com.moujitx.homebox.server.enums.InvoiceStatus;
import com.moujitx.homebox.server.enums.InvoiceType;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.InvoiceAttachmentRepository;
import com.moujitx.homebox.server.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceAttachmentRepository attachmentRepository;
    private final FileService fileService;
    private final InvoiceParseService invoiceParseService;

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getInvoices(String search, InvoiceType invoiceType, InvoiceStatus invoiceStatus,
            String buyerName, String sellerName, Pageable pageable) {
        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;
        Page<Invoice> page = invoiceRepository.findWithFilters(searchParam, invoiceType, invoiceStatus,
                buyerName, sellerName, pageable);
        List<InvoiceResponse> responses = page.getContent().stream()
                .map(InvoiceResponse::from)
                .toList();
        return new PageImpl<>(responses, pageable, page.getTotalElements());
    }

    @Transactional
    public InvoiceDetailResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        if (invoice.getPreviewImage() == null && invoice.getFile() != null) {
            generateAndSavePreview(invoice);
        }

        return InvoiceDetailResponse.from(invoice);
    }

    @Transactional
    public InvoiceDetailResponse createInvoice(CreateInvoiceRequest request) {
        if (request.getInvoiceNumber() != null && !request.getInvoiceNumber().isBlank()
                && invoiceRepository.existsByInvoiceNumber(request.getInvoiceNumber())) {
            throw new ResourceAlreadyExistsException("Invoice number already exists: " + request.getInvoiceNumber());
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(request.getInvoiceNumber());
        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setInvoiceType(request.getInvoiceType());
        invoice.setInvoiceStatus(
                request.getInvoiceStatus() != null ? request.getInvoiceStatus() : InvoiceStatus.NORMAL);
        invoice.setSellerName(request.getSellerName());
        invoice.setSellerTaxId(request.getSellerTaxId());
        invoice.setBuyerName(request.getBuyerName());
        invoice.setBuyerTaxId(request.getBuyerTaxId());
        invoice.setAmount(request.getAmount());
        invoice.setTaxAmount(request.getTaxAmount());
        invoice.setTotalAmount(request.getTotalAmount());
        invoice.setRemark(request.getRemark());
        invoice.setPreviewImage(request.getPreviewImage());

        if (request.getFileId() != null) {
            FileRecord file = fileService.getFileById(request.getFileId());
            invoice.setFile(file);
        }

        Invoice saved = invoiceRepository.save(invoice);
        return InvoiceDetailResponse.from(saved);
    }

    @Transactional
    public InvoiceParseResponse parseInvoice(MultipartFile file) {
        try {
            FileRecord fileRecord = fileService.upload(file);
            byte[] content = fileService.loadFileContent(fileRecord);
            InvoiceParseResponse result = invoiceParseService.parse(content, file.getOriginalFilename());
            result.setFileId(fileRecord.getId());
            return result;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse invoice: " + e.getMessage(), e);
        }
    }

    @Transactional
    public InvoiceDetailResponse updateInvoice(Long id, UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        if (request.getInvoiceNumber() != null) {
            if (!request.getInvoiceNumber().isBlank()
                    && invoiceRepository.existsByInvoiceNumberAndIdNot(request.getInvoiceNumber(), id)) {
                throw new ResourceAlreadyExistsException(
                        "Invoice number already exists: " + request.getInvoiceNumber());
            }
            invoice.setInvoiceNumber(request.getInvoiceNumber());
        }
        if (request.getInvoiceDate() != null)
            invoice.setInvoiceDate(request.getInvoiceDate());
        if (request.getInvoiceType() != null)
            invoice.setInvoiceType(request.getInvoiceType());
        if (request.getInvoiceStatus() != null)
            invoice.setInvoiceStatus(request.getInvoiceStatus());
        if (request.getSellerName() != null)
            invoice.setSellerName(request.getSellerName());
        if (request.getSellerTaxId() != null)
            invoice.setSellerTaxId(request.getSellerTaxId());
        if (request.getBuyerName() != null)
            invoice.setBuyerName(request.getBuyerName());
        if (request.getBuyerTaxId() != null)
            invoice.setBuyerTaxId(request.getBuyerTaxId());
        if (request.getAmount() != null)
            invoice.setAmount(request.getAmount());
        if (request.getTaxAmount() != null)
            invoice.setTaxAmount(request.getTaxAmount());
        if (request.getTotalAmount() != null)
            invoice.setTotalAmount(request.getTotalAmount());
        if (request.getRemark() != null)
            invoice.setRemark(request.getRemark().isEmpty() ? null : request.getRemark());

        Invoice saved = invoiceRepository.save(invoice);
        return InvoiceDetailResponse.from(saved);
    }

    @Transactional
    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        for (var attachment : invoice.getAttachments()) {
            fileService.delete(attachment.getFile().getId());
        }

        if (invoice.getFile() != null) {
            fileService.delete(invoice.getFile().getId());
        }

        invoiceRepository.delete(invoice);
    }

    @Transactional
    public InvoiceAttachmentResponse uploadAttachment(Long invoiceId, MultipartFile file) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        FileRecord fileRecord = fileService.upload(file);

        InvoiceAttachment attachment = new InvoiceAttachment();
        attachment.setInvoice(invoice);
        attachment.setFile(fileRecord);

        return InvoiceAttachmentResponse.from(attachmentRepository.save(attachment));
    }

    @Transactional
    public void deleteAttachment(Long invoiceId, Long attachmentId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + invoiceId);
        }

        InvoiceAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));

        if (!attachment.getInvoice().getId().equals(invoiceId)) {
            throw new ResourceNotFoundException(
                    "Attachment not found with id: " + attachmentId + " for invoice: " + invoiceId);
        }

        Long fileId = attachment.getFile().getId();
        attachmentRepository.delete(attachment);
        fileService.delete(fileId);
    }

    public FileRecord getInvoiceFile(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
        if (invoice.getFile() == null) {
            throw new ResourceNotFoundException("No file associated with invoice id: " + invoiceId);
        }
        return invoice.getFile();
    }

    public FileRecord getAttachmentFile(Long invoiceId, Long attachmentId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + invoiceId);
        }
        InvoiceAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));
        if (!attachment.getInvoice().getId().equals(invoiceId)) {
            throw new ResourceNotFoundException(
                    "Attachment not found with id: " + attachmentId + " for invoice: " + invoiceId);
        }
        return attachment.getFile();
    }

    private void generateAndSavePreview(Invoice invoice) {
        try {
            FileRecord file = invoice.getFile();
            String contentType = file.getContentType();
            if (contentType == null)
                return;

            byte[] content = fileService.loadFileContent(file);
            String previewImage = null;

            if (contentType.contains("pdf")) {
                previewImage = invoiceParseService.renderPdfPreview(content);
            } else if (contentType.contains("ofd")) {
                previewImage = invoiceParseService.renderOfdPreview(content);
            }

            if (previewImage != null) {
                invoice.setPreviewImage(previewImage);
                invoiceRepository.save(invoice);
            }
        } catch (Exception e) {
            log.warn("Failed to auto-generate preview for invoice {}", invoice.getId(), e);
        }
    }
}
