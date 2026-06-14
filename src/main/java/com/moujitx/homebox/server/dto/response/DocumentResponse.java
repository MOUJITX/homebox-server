package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Document;
import com.moujitx.homebox.server.enums.DocumentStatus;
import com.moujitx.homebox.server.enums.Importance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Long parentId;
    private String parentName;
    private String holder;
    private String documentNumber;
    private String issuer;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private DocumentStatus status;
    private Importance importance;
    private Integer reminderDays;
    private String note;
    private int subDocumentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Setter
    private boolean hasInvoice;

    public static DocumentResponse from(Document document, int subDocumentCount) {
        DocumentResponse response = new DocumentResponse();
        populateFromDocument(response, document, subDocumentCount);
        return response;
    }

    public static DocumentResponse from(Document document, Map<Long, Integer> subDocumentCounts) {
        int subDocumentCount = subDocumentCounts.getOrDefault(document.getId(), 0);
        DocumentResponse response = new DocumentResponse();
        populateFromDocument(response, document, subDocumentCount);
        return response;
    }

    protected static void populateFromDocument(DocumentResponse response, Document document, int subDocumentCount) {
        response.id = document.getId();
        response.name = document.getName();
        response.categoryId = document.getCategory().getId();
        response.categoryName = document.getCategory().getName();
        response.holder = document.getHolder();
        response.documentNumber = document.getDocumentNumber();
        response.issuer = document.getIssuer();
        response.issueDate = document.getIssueDate();
        response.expiryDate = document.getExpiryDate();
        response.status = document.getStatus();
        response.importance = document.getImportance();
        response.reminderDays = document.getReminderDays();
        response.note = document.getNote();
        response.subDocumentCount = subDocumentCount;
        response.createdAt = document.getCreatedAt();
        response.updatedAt = document.getUpdatedAt();

        if (document.getParent() != null) {
            response.parentId = document.getParent().getId();
            response.parentName = document.getParent().getName();
        }
    }
}
