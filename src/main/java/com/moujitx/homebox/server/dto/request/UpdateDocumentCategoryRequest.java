package com.moujitx.homebox.server.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDocumentCategoryRequest {

    private String name;

    private String description;
}
