package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.SystemConfig;

public record SystemConfigResponse(
        String key,
        String value,
        boolean sensitive,
        String description
) {
    public static SystemConfigResponse from(SystemConfig entity, String maskedValue) {
        return new SystemConfigResponse(
                entity.getConfigKey(),
                entity.isSensitive() ? maskedValue : entity.getConfigValue(),
                entity.isSensitive(),
                entity.getDescription()
        );
    }
}
