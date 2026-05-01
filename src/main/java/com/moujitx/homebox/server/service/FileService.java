package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.FileRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRecordRepository fileRecordRepository;
    private final FileStorageStrategy fileStorageStrategy;

    @Transactional
    public FileRecord upload(MultipartFile file) {
        String storedFilename = fileStorageStrategy.store(file);

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
        return fileStorageStrategy.load(record.getStoredFilename());
    }

    public byte[] loadFileContent(FileRecord record) {
        return fileStorageStrategy.load(record.getStoredFilename());
    }

    public Page<FileRecord> listFiles(Pageable pageable) {
        return fileRecordRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional
    public FileRecord rename(Long id, String originalFilename) {
        FileRecord record = getFileById(id);
        record.setOriginalFilename(originalFilename);
        return fileRecordRepository.save(record);
    }

    @Transactional
    public void delete(Long id) {
        FileRecord record = getFileById(id);
        fileStorageStrategy.delete(record.getStoredFilename());
        fileRecordRepository.delete(record);
    }
}
