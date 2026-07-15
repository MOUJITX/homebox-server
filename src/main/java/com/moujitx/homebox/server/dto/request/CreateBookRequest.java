package com.moujitx.homebox.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CreateBookRequest {

    @NotBlank
    private String title;

    private String author;

    private String isbn;

    private boolean serialized;

    private Long parentId;

    private String issueNumber;

    private String publisher;

    private LocalDate publishDate;

    private String description;

    @NotBlank
    private Long categoryId;

    @NotBlank
    private Long locationId;

    private String status;

    private LocalDate purchaseDate;

    private BigDecimal purchasePrice;

    private String note;

    private List<Long> seriesIds;
}
