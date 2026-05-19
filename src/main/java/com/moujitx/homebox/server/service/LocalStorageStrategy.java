package com.moujitx.homebox.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
public class LocalStorageStrategy implements FileStorageStrategy {

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;
    private static final DateTimeFormatter DATE_DIR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final Path uploadDirectory;

    public LocalStorageStrategy(String uploadDir) {
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
        log.info("Using local file storage: {}", this.uploadDirectory);
    }

    @Override
    public String store(MultipartFile file) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = UUID.randomUUID() + extension;

        String dateDir = LocalDate.now().format(DATE_DIR_FORMAT);
        Path targetDir = uploadDirectory.resolve(dateDir);
        try {
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(storedFilename);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return dateDir + "/" + storedFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public byte[] load(String storedFilename) {
        try {
            Path path = resolvePath(storedFilename);
            if (!Files.exists(path)) {
                throw new RuntimeException("File not found: " + storedFilename);
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + storedFilename, e);
        }
    }

    @Override
    public void delete(String storedFilename) {
        try {
            Path path = resolvePath(storedFilename);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + storedFilename, e);
        }
    }

    private Path resolvePath(String storedFilename) {
        Path resolved = uploadDirectory.resolve(storedFilename).normalize();
        if (!resolved.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Invalid file path");
        }
        return resolved;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 100MB");
        }
    }
}
