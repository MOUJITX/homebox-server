package com.moujitx.homebox.server.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateGoodItemRequest {

    private LocalDate productDate;

    private LocalDate expirationDate;

    private Integer lifeDays;

    private Boolean inUse;
}
