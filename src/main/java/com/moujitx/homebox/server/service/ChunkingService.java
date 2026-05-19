package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.entity.TextChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    private static final int CHUNK_SIZE = 500;
    private static final int OVERLAP = 50;

    public List<TextChunk> chunk(List<TextChunk> extractedChunks) {
        List<TextChunk> result = new ArrayList<>();
        int globalIndex = 0;

        for (TextChunk source : extractedChunks) {
            Integer pageNumber = source.getPageNumber();
            String text = source.getChunkText();

            if (text.length() <= CHUNK_SIZE) {
                TextChunk chunk = new TextChunk();
                chunk.setFileId(source.getFileId());
                chunk.setChunkIndex(globalIndex++);
                chunk.setChunkText(text);
                chunk.setPageNumber(pageNumber);
                chunk.setTokenCount(text.length());
                result.add(chunk);
            } else {
                List<String> fragments = splitText(text, CHUNK_SIZE, OVERLAP);
                for (String fragment : fragments) {
                    TextChunk chunk = new TextChunk();
                    chunk.setFileId(source.getFileId());
                    chunk.setChunkIndex(globalIndex++);
                    chunk.setChunkText(fragment);
                    chunk.setPageNumber(pageNumber);
                    chunk.setTokenCount(fragment.length());
                    result.add(chunk);
                }
            }
        }

        return result;
    }

    private List<String> splitText(String text, int chunkSize, int overlap) {
        List<String> fragments = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());

            if (end < text.length()) {
                int breakPoint = findBreakPoint(text, end, overlap);
                if (breakPoint > start) {
                    end = breakPoint;
                }
            }

            fragments.add(text.substring(start, end).trim());
            start = end - overlap;
            if (start >= text.length() || start <= 0) {
                break;
            }
        }

        return fragments.isEmpty() ? List.of(text) : fragments;
    }

    private int findBreakPoint(String text, int end, int overlap) {
        int searchEnd = Math.min(text.length(), end);
        int searchStart = Math.max(0, end - overlap);

        for (int i = searchEnd - 1; i >= searchStart; i--) {
            char c = text.charAt(i);
            if (c == '\n' || c == '。' || c == '；' || c == '。' || c == '\r') {
                return i + 1;
            }
        }

        for (int i = searchEnd - 1; i >= searchStart; i--) {
            char c = text.charAt(i);
            if (c == '.' || c == ';' || c == '!' || c == '?' || c == ' ') {
                return i + 1;
            }
        }

        return end;
    }
}
