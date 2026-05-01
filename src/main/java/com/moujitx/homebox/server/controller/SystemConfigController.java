package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.response.SystemConfigGroupResponse;
import com.moujitx.homebox.server.dto.response.TestConnectionResponse;
import com.moujitx.homebox.server.service.AiService;
import com.moujitx.homebox.server.service.FileStorageStrategyProvider;
import com.moujitx.homebox.server.service.QiniuStorageStrategy;
import com.moujitx.homebox.server.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/system-config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;
    private final FileStorageStrategyProvider fileStorageStrategyProvider;
    private final AiService aiService;

    @GetMapping
    public ResponseEntity<SystemConfigGroupResponse> getByGroup(@RequestParam String group) {
        return ResponseEntity.ok(systemConfigService.getByGroup(group));
    }

    @PutMapping("/{group}")
    public ResponseEntity<Void> saveGroup(@PathVariable String group, @RequestBody Map<String, String> values) {
        systemConfigService.saveGroup(group, values);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test/qiniu")
    public ResponseEntity<TestConnectionResponse> testQiniuConnection() {
        try {
            var strategy = fileStorageStrategyProvider.getStrategy();
            if (strategy instanceof QiniuStorageStrategy qiniu) {
                qiniu.testConnection();
                return ResponseEntity.ok(new TestConnectionResponse(true, "Qiniu connection successful"));
            }
            return ResponseEntity.ok(new TestConnectionResponse(false, "Qiniu storage is not configured"));
        } catch (Exception e) {
            return ResponseEntity.ok(new TestConnectionResponse(false, "Qiniu connection failed: " + e.getMessage()));
        }
    }

    @PostMapping("/test/ai")
    public ResponseEntity<TestConnectionResponse> testAiConnection() {
        return ResponseEntity.ok(aiService.testConnection());
    }
}
