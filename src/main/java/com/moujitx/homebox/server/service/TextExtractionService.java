package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.entity.TextChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TextExtractionService {

    private final FileStorageStrategyProvider strategyProvider;
    private final Tika tika = new Tika();

    public List<TextChunk> extract(FileRecord fileRecord) {
        String contentType = fileRecord.getContentType();
        String filename = fileRecord.getOriginalFilename();

        try {
            byte[] content = strategyProvider.getStrategy().load(fileRecord.getStoredFilename());

            if (isPdf(contentType, filename)) {
                return extractPdf(content, fileRecord.getId());
            } else {
                return extractWithTika(content, fileRecord.getId(), filename);
            }
        } catch (Exception e) {
            log.warn("Text extraction failed for file {} ({}): {}", fileRecord.getId(), filename, e.getMessage());
            return List.of();
        }
    }

    private boolean isPdf(String contentType, String filename) {
        if (contentType != null && contentType.equalsIgnoreCase("application/pdf")) {
            return true;
        }
        if (filename != null) {
            String lower = filename.toLowerCase();
            return lower.endsWith(".pdf");
        }
        return false;
    }

    private List<TextChunk> extractPdf(byte[] content, Long fileId) throws IOException {
        List<TextChunk> chunks = new ArrayList<>();
        try (PDDocument document = PDDocument.load(content)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int pageCount = document.getNumberOfPages();

            for (int page = 1; page <= pageCount; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String pageText = stripper.getText(document);

                if (pageText != null && !pageText.isBlank()) {
                    TextChunk chunk = new TextChunk();
                    chunk.setFileId(fileId);
                    chunk.setChunkIndex(chunks.size());
                    chunk.setChunkText(pageText.trim());
                    chunk.setPageNumber(page);
                    chunk.setTokenCount(pageText.length());
                    chunks.add(chunk);
                }
            }
        }
        return chunks;
    }

    private List<TextChunk> extractWithTika(byte[] content, Long fileId, String filename) {
        try {
            String text = tika.parseToString(new java.io.ByteArrayInputStream(content));
            if (text != null && !text.isBlank()) {
                TextChunk chunk = new TextChunk();
                chunk.setFileId(fileId);
                chunk.setChunkIndex(0);
                chunk.setChunkText(text.trim());
                chunk.setPageNumber(null);
                chunk.setTokenCount(text.length());
                return List.of(chunk);
            }
        } catch (IOException | TikaException e) {
            log.warn("Tika extraction failed for file {} ({}): {}", fileId, filename, e.getMessage());
        }
        return List.of();
    }
}
