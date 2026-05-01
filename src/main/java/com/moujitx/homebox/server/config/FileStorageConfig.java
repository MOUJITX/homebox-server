package com.moujitx.homebox.server.config;

import com.moujitx.homebox.server.service.FileStorageStrategy;
import com.moujitx.homebox.server.service.LocalStorageStrategy;
import com.moujitx.homebox.server.service.QiniuStorageStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileStorageConfig {

    @Value("${app.upload.directory:uploads}")
    private String uploadDirectory;

    @Value("${app.qiniu.access-key:}")
    private String accessKey;

    @Value("${app.qiniu.secret-key:}")
    private String secretKey;

    @Value("${app.qiniu.bucket:}")
    private String bucket;

    @Value("${app.qiniu.folder:}")
    private String folder;

    @Value("${app.qiniu.domain:}")
    private String domain;

    @Bean
    public FileStorageStrategy fileStorageStrategy() {
        if (accessKey != null && !accessKey.isBlank()) {
            return new QiniuStorageStrategy(accessKey, secretKey, bucket, folder, domain);
        }
        return new LocalStorageStrategy(uploadDirectory);
    }
}
