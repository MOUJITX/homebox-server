package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.entity.TextChunk;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.FileRecordRepository;
import com.moujitx.homebox.server.repository.TextChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final FileRecordRepository fileRecordRepository;
    private final TextChunkRepository textChunkRepository;
    private final FileStorageStrategyProvider strategyProvider;
    private final TextExtractionService textExtractionService;
    private final ChunkingService chunkingService;
    private final EsIndexService esIndexService;
    private final EsClientProvider esClientProvider;

    @Transactional
    public FileRecord upload(MultipartFile file) {
        String storedFilename = strategyProvider.getStrategy().store(file);

        FileRecord record = new FileRecord();
        record.setStoredFilename(storedFilename);
        record.setOriginalFilename(file.getOriginalFilename());
        record.setContentType(file.getContentType());
        record.setFileSize(file.getSize());

        FileRecord saved = fileRecordRepository.save(record);
        extractAndIndexAsync(saved);
        return saved;
    }

    @Async("textExtractionExecutor")
    public void extractAndIndexAsync(FileRecord fileRecord) {
        try {
            List<TextChunk> extracted = textExtractionService.extract(fileRecord);
            List<TextChunk> chunks = chunkingService.chunk(extracted);
            chunks = textChunkRepository.saveAll(chunks);
            if (esClientProvider.isAvailable()) {
                boolean indexed = esIndexService.indexChunks(chunks);
                if (indexed) {
                    for (TextChunk chunk : chunks) {
                        chunk.setIndexed(true);
                    }
                    textChunkRepository.saveAll(chunks);
                }
            }
        } catch (Exception e) {
            log.warn("Async text extraction failed for file {}: {}", fileRecord.getId(), e.getMessage());
        }
    }

    public FileRecord getFileById(Long id) {
        return fileRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));
    }

    public byte[] loadFileContent(Long id) {
        FileRecord record = getFileById(id);
        return strategyProvider.getStrategy().load(record.getStoredFilename());
    }

    public byte[] loadFileContent(FileRecord record) {
        return strategyProvider.getStrategy().load(record.getStoredFilename());
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
        esIndexService.deleteByFileId(id);
        textChunkRepository.deleteByFileId(id);
        fileRecordRepository.delete(record);
        fileRecordRepository.flush();
        strategyProvider.getStrategy().delete(record.getStoredFilename());
    }

    public boolean isIndexed(Long fileId) {
        List<TextChunk> chunks = textChunkRepository.findByFileIdOrderByChunkIndexAsc(fileId);
        return !chunks.isEmpty() && chunks.stream().allMatch(TextChunk::isIndexed);
    }

}
