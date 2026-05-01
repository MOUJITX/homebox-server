package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class FileStorageStrategyProvider implements CommandLineRunner {

    private final SystemConfigRepository systemConfigRepository;

    @Value("${app.upload.directory:uploads}")
    private String uploadDirectory;

    private volatile FileStorageStrategy currentStrategy;

    @Override
    public void run(String... args) {
        reload();
    }

    public FileStorageStrategy getStrategy() {
        return currentStrategy;
    }

    public void reload() {
        String accessKey = getConfigValue("qiniu.access-key");
        String secretKey = getConfigValue("qiniu.secret-key");
        String bucket = getConfigValue("qiniu.bucket");
        String folder = getConfigValue("qiniu.folder");
        String domain = getConfigValue("qiniu.domain");

        reload(accessKey, secretKey, bucket, folder, domain);
    }

    public void reload(String accessKey, String secretKey, String bucket, String folder, String domain) {
        if (accessKey != null && !accessKey.isBlank()) {
            this.currentStrategy = new QiniuStorageStrategy(accessKey, secretKey, bucket, folder, domain);
        } else {
            this.currentStrategy = new LocalStorageStrategy(uploadDirectory);
        }
    }

    private String getConfigValue(String key) {
        return systemConfigRepository.findByConfigKey(key)
                .map(c -> c.getConfigValue())
                .orElse("");
    }
}
