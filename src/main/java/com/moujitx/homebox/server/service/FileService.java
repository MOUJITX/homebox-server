package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.FileRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private final FileRecordRepository fileRecordRepository;
    private final FileStorageService fileStorageService;

    public FileService(FileRecordRepository fileRecordRepository,
                       FileStorageService fileStorageService) {
        this.fileRecordRepository = fileRecordRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public FileRecord upload(MultipartFile file) {
        String storedFilename = fileStorageService.store(file);

        FileRecord record = new FileRecord();
        record.setStoredFilename(storedFilename);
        record.setOriginalFilename(file.getOriginalFilename());
        record.setContentType(file.getContentType());
        record.setFileSize(file.getSize());

        return fileRecordRepository.save(record);
    }

    public FileRecord getFileById(Long id) {
        return fileRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));
    }

    public byte[] loadFileContent(Long id) {
        FileRecord record = getFileById(id);
        return fileStorageService.load(record.getStoredFilename());
    }

    @Transactional
    public void delete(Long id) {
        FileRecord record = getFileById(id);
        fileStorageService.delete(record.getStoredFilename());
        fileRecordRepository.delete(record);
    }
}
