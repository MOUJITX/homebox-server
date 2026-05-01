package com.moujitx.homebox.server.service;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import com.moujitx.homebox.server.exception.ResourceNotFoundException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
public class QiniuStorageStrategy implements FileStorageStrategy {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final DateTimeFormatter DATE_DIR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final String bucket;
    private final String folder;
    private final String domain;
    private final Auth auth;
    private final UploadManager uploadManager;
    private final BucketManager bucketManager;

    public QiniuStorageStrategy(String accessKey, String secretKey, String bucket, String folder, String domain) {
        this.bucket = bucket;
        this.folder = StringUtils.isNullOrEmpty(folder) ? "" : folder.endsWith("/") ? folder : folder + "/";
        this.domain = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
        this.auth = Auth.create(accessKey, secretKey);
        Configuration cfg = new Configuration();
        this.uploadManager = new UploadManager(cfg);
        this.bucketManager = new BucketManager(auth, cfg);
        log.info("Using Qiniu OSS storage: bucket={}, folder={}", bucket, this.folder);
    }

    @Override
    public String store(MultipartFile file) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String dateDir = LocalDate.now().format(DATE_DIR_FORMAT);
        String key = folder + dateDir + "/" + UUID.randomUUID() + extension;
        String token = auth.uploadToken(bucket);

        try {
            Response response = uploadManager.put(file.getInputStream(), key, token, null, null);
            if (!response.isOK()) {
                throw new RuntimeException("Failed to upload to Qiniu: " + response.error);
            }
            return key;
        } catch (QiniuException e) {
            throw new RuntimeException("Failed to upload to Qiniu", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file for upload", e);
        }
    }

    @Override
    public byte[] load(String storedFilename) {
        String url = domain + "/" + storedFilename;
        String privateUrl = auth.privateDownloadUrl(url);

        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(privateUrl))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new ResourceNotFoundException("Failed to download from Qiniu, status: " + response.statusCode() + ", filename: " + storedFilename);
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to download from Qiniu: " + storedFilename, e);
        }
    }

    @Override
    public void delete(String storedFilename) {
        try {
            Response response = bucketManager.delete(bucket, storedFilename);
            if (!response.isOK()) {
                log.warn("Failed to delete from Qiniu (status={}): {}", response.statusCode, response.error);
            }
        } catch (QiniuException e) {
            log.warn("Failed to delete from Qiniu (file may not exist): {}", storedFilename, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }
    }
}
