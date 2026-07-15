package com.moujitx.homebox.server.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBookCategoryRequest {

    private String name;

    private String key;

    private Boolean serialized;

    private String description;
}
