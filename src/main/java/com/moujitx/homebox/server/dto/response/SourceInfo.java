package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.enums.SourceType;
import lombok.Getter;

@Getter
public class SourceInfo {

    private SourceType type;
    private String typeLabel;
    private Long sourceId;
    private String sourceName;

    public SourceInfo(SourceType type, String typeLabel, Long sourceId, String sourceName) {
        this.type = type;
        this.typeLabel = typeLabel;
        this.sourceId = sourceId;
        this.sourceName = sourceName;
    }
}
