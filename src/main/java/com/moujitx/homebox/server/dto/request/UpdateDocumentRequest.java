package com.moujitx.homebox.server.dto.request;

import com.moujitx.homebox.server.enums.DocumentStatus;
import com.moujitx.homebox.server.enums.Importance;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateDocumentRequest {

    private String name;

    private Long categoryId;

    private String holder;

    private String documentNumber;

    private String issuer;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    private DocumentStatus status;

    private Importance importance;

    private Integer reminderDays;

    private String note;
}
