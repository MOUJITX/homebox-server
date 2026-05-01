package com.moujitx.homebox.server.dto.response;

import java.util.List;

public record SystemConfigGroupResponse(
        String group,
        List<SystemConfigResponse> items
) {
}
