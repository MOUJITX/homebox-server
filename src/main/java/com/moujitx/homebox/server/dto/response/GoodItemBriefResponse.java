package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.GoodItem;
import com.moujitx.homebox.server.enums.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class GoodItemBriefResponse {

    private Long id;
    private LocalDate expirationDate;
    private boolean inUse;
    private ItemStatus status;
    private long daysUntilExpiration;

    public static GoodItemBriefResponse from(GoodItem item, ItemStatus status, long daysUntilExpiration) {
        return new GoodItemBriefResponse(
                item.getId(),
                item.getExpirationDate(),
                item.isInUse(),
                status,
                daysUntilExpiration
        );
    }
}
