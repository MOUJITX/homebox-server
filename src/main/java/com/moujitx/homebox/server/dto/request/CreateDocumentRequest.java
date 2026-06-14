package com.moujitx.homebox.server.dto.request;

import com.moujitx.homebox.server.enums.DocumentStatus;
import com.moujitx.homebox.server.enums.Importance;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateDocumentRequest {

    @NotBlank
    private String name;

    @NotNull
    private Long categoryId;

    private Long parentId;

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
