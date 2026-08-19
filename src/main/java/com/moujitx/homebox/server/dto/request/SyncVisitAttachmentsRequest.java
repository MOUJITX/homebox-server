package com.moujitx.homebox.server.dto.request;

import com.moujitx.homebox.server.enums.VisitSourceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SyncVisitAttachmentsRequest {

    private VisitSourceType sourceType;

    private Long sourceId;

    private List<Long> fileIds;
}
