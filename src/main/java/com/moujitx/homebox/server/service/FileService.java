package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.entity.TextChunk;
import com.moujitx.homebox.server.enums.ProcessStatus;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.*;
import com.moujitx.homebox.server.util.StringUtil;
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
    private final GoodPictureRepository goodPictureRepository;
    private final GoodAttachmentRepository goodAttachmentRepository;
    private final AssetPictureRepository assetPictureRepository;
    private final AssetAttachmentRepository assetAttachmentRepository;
    private final InvoiceAttachmentRepository invoiceAttachmentRepository;
    private final VisitAttachmentRepository visitAttachmentRepository;
    private final SubscriptionRecordAttachmentRepository subscriptionRecordAttachmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PlatformRepository platformRepository;
    private final PaymentMethodRepository paymentMethodRepository;

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
        extractAndIndex(fileRecord.getId());
    }

    public void extractAndIndex(Long fileId) {
        FileRecord fileRecord = getFileById(fileId);
        ProcessStatus extractStatus = fileRecord.getExtractStatus();
        ProcessStatus chunkStatus = fileRecord.getChunkStatus();

        boolean needExtract = extractStatus == ProcessStatus.PENDING || extractStatus == ProcessStatus.FAILED;
        boolean needChunk = chunkStatus == ProcessStatus.PENDING || chunkStatus == ProcessStatus.FAILED;

        if (needExtract) {
            fileRecord.setExtractStatus(ProcessStatus.PROCESSING);
            fileRecordRepository.save(fileRecord);
            try {
                List<TextChunk> extracted = textExtractionService.extract(fileRecord);
                if (extracted.isEmpty()) {
                    fileRecord.setExtractStatus(ProcessStatus.FAILED);
                    fileRecordRepository.save(fileRecord);
                    return;
                }
                fileRecord.setExtractStatus(ProcessStatus.SUCCESS);
                fileRecordRepository.save(fileRecord);
                runChunkAndIndex(fileRecord, extracted);
            } catch (Exception e) {
                log.warn("Text extraction failed for file {}: {}", fileRecord.getId(), e.getMessage());
                fileRecord.setExtractStatus(ProcessStatus.FAILED);
                fileRecordRepository.save(fileRecord);
            }
        } else if (needChunk) {
            List<TextChunk> extracted = textChunkRepository.findByFileIdOrderByChunkIndexAsc(fileRecord.getId());
            runChunkAndIndex(fileRecord, extracted);
        }
    }

    private void runChunkAndIndex(FileRecord fileRecord, List<TextChunk> extracted) {
        fileRecord.setChunkStatus(ProcessStatus.PROCESSING);
        fileRecordRepository.save(fileRecord);
        try {
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
            fileRecord.setChunkStatus(ProcessStatus.SUCCESS);
            fileRecordRepository.save(fileRecord);
        } catch (Exception e) {
            log.warn("Chunk/index failed for file {}: {}", fileRecord.getId(), e.getMessage());
            fileRecord.setChunkStatus(ProcessStatus.FAILED);
            fileRecordRepository.save(fileRecord);
        }
    }

    @Async("textExtractionExecutor")
    public void retryAsync(Long fileId) {
        extractAndIndex(fileId);
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

    public Page<FileRecord> listFiles(String search, String contentType, String status, Pageable pageable) {
        String searchParam = StringUtil.normalizeSearch(search);
        String contentTypeParam = (contentType != null && !contentType.isBlank()) ? contentType.trim() : null;
        String statusParam = (status != null && !status.isBlank()) ? status.trim().toUpperCase() : null;
        return fileRecordRepository.findWithFilters(searchParam, contentTypeParam, statusParam, pageable);
    }

    @Transactional
    public FileRecord rename(Long id, String originalFilename) {
        FileRecord record = getFileById(id);
        record.setOriginalFilename(originalFilename);
        return fileRecordRepository.save(record);
    }

    @Transactional
    public void delete(Long id) {
        if (isFileReferenced(id)) {
            throw new IllegalStateException("File is still referenced by other entities and cannot be deleted");
        }
        FileRecord record = getFileById(id);
        esIndexService.deleteByFileId(id);
        textChunkRepository.deleteByFileId(id);
        fileRecordRepository.delete(record);
        fileRecordRepository.flush();
        strategyProvider.getStrategy().delete(record.getStoredFilename());
    }

    /**
     * Check if a file is referenced by any entity (picture, attachment, invoice, platform, payment method).
     */
    public boolean isFileReferenced(Long fileId) {
        return goodPictureRepository.existsByFileId(fileId)
                || goodAttachmentRepository.existsByFileId(fileId)
                || assetPictureRepository.existsByFileId(fileId)
                || assetAttachmentRepository.existsByFileId(fileId)
                || invoiceAttachmentRepository.existsByFileId(fileId)
                || visitAttachmentRepository.existsByFileId(fileId)
                || subscriptionRecordAttachmentRepository.existsByFileId(fileId)
                || invoiceRepository.existsByFileId(fileId)
                || platformRepository.existsByLogoFileId(fileId)
                || paymentMethodRepository.existsByLogoFileId(fileId);
    }

    /**
     * Delete a file only if it is not referenced by any entity.
     * Returns true if the file was deleted, false if it is still in use.
     */
    @Transactional
    public boolean deleteIfUnused(Long fileId) {
        if (isFileReferenced(fileId)) {
            return false;
        }
        delete(fileId);
        return true;
    }

    public boolean isIndexed(Long fileId) {
        List<TextChunk> chunks = textChunkRepository.findByFileIdOrderByChunkIndexAsc(fileId);
        return !chunks.isEmpty() && chunks.stream().allMatch(TextChunk::isIndexed);
    }

}
