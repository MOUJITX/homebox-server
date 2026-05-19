package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.TextChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TextChunkRepository extends JpaRepository<TextChunk, Long> {

    List<TextChunk> findByFileIdOrderByChunkIndexAsc(Long fileId);

    void deleteByFileId(Long fileId);

    List<TextChunk> findByIndexedFalse();
}
