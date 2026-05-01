package com.moujitx.homebox.server.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageStrategy {

    String store(MultipartFile file);

    byte[] load(String storedFilename);

    void delete(String storedFilename);
}
