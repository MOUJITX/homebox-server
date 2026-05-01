package com.moujitx.homebox.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAssetCategoryRequest {

    @NotBlank
    private String name;

    private String description;
}
