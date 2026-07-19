package com.moujitx.homebox.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBookCategoryRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String key;

    private boolean serialized;

    private String description;
}
