package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.response.SystemConfigGroupResponse;
import com.moujitx.homebox.server.dto.response.SystemConfigResponse;
import com.moujitx.homebox.server.entity.SystemConfig;
import com.moujitx.homebox.server.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;
    private final FileStorageStrategyProvider fileStorageStrategyProvider;

    public SystemConfigGroupResponse getByGroup(String group) {
        List<SystemConfig> configs = systemConfigRepository.findByConfigGroup(group);
        List<SystemConfigResponse> items = configs.stream()
                .map(c -> SystemConfigResponse.from(c, maskValue(c.getConfigValue())))
                .toList();
        return new SystemConfigGroupResponse(group, items);
    }

    public String get(String key) {
        return systemConfigRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElse("");
    }

    @Transactional
    public void saveGroup(String group, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            SystemConfig config = systemConfigRepository.findByConfigKey(key).orElse(null);
            if (config == null) {
                continue;
            }

            // Skip masked values that weren't changed
            if (config.isSensitive() && isMaskedValue(value)) {
                continue;
            }

            config.setConfigValue(value);
            systemConfigRepository.save(config);
        }

        // Trigger hot-reload for qiniu config
        if ("qiniu".equals(group)) {
            fileStorageStrategyProvider.reload();
        }
    }

    private String maskValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }

    private boolean isMaskedValue(String value) {
        return value != null && value.contains("****");
    }
}
