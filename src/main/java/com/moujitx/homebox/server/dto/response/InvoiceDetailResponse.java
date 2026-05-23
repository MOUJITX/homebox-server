package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Invoice;
import com.moujitx.homebox.server.enums.InvoiceStatus;
import com.moujitx.homebox.server.enums.InvoiceType;
import com.moujitx.homebox.server.util.OssUrlBuilder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class InvoiceDetailResponse {

    private Long id;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private InvoiceType invoiceType;
    private InvoiceStatus invoiceStatus;
    private String sellerName;
    private String sellerTaxId;
    private String buyerName;
    private String buyerTaxId;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String remark;
    private Long fileId;
    private String fileUrl;
    private String previewImage;
    private List<InvoiceAttachmentResponse> attachments;
    private List<BoundAssetResponse> assets;
    @Setter
    private List<BoundSubscriptionResponse> subscriptions;
    @Setter
    private List<BoundVisitResponse> visits;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InvoiceDetailResponse from(Invoice invoice) {
        return from(invoice, List.of());
    }

    public static InvoiceDetailResponse from(Invoice invoice, List<BoundAssetResponse> boundAssets) {
        InvoiceDetailResponse response = new InvoiceDetailResponse();
        response.id = invoice.getId();
        response.invoiceNumber = invoice.getInvoiceNumber();
        response.invoiceDate = invoice.getInvoiceDate();
        response.invoiceType = invoice.getInvoiceType();
        response.invoiceStatus = invoice.getInvoiceStatus();
        response.sellerName = invoice.getSellerName();
        response.sellerTaxId = invoice.getSellerTaxId();
        response.buyerName = invoice.getBuyerName();
        response.buyerTaxId = invoice.getBuyerTaxId();
        response.amount = invoice.getAmount();
        response.taxAmount = invoice.getTaxAmount();
        response.totalAmount = invoice.getTotalAmount();
        response.remark = invoice.getRemark();
        response.previewImage = invoice.getPreviewImage();
        response.createdAt = invoice.getCreatedAt();
        response.updatedAt = invoice.getUpdatedAt();

        if (invoice.getFile() != null) {
            response.fileId = invoice.getFile().getId();
            response.fileUrl = OssUrlBuilder.build(invoice.getFile().getStoredFilename(), invoice.getFile().getOriginalFilename());
        }

        response.attachments = invoice.getAttachments().stream()
                .map(InvoiceAttachmentResponse::from)
                .toList();
        response.assets = boundAssets;

        return response;
    }
}
