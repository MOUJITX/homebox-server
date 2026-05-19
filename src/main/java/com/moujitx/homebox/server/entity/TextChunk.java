package com.moujitx.homebox.server.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "text_chunks", uniqueConstraints = @UniqueConstraint(columnNames = {"file_id", "chunk_index"}))
@Getter
@Setter
@NoArgsConstructor
public class TextChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(name = "chunk_text", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String chunkText;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(nullable = false)
    private boolean indexed = false;

    @Setter(AccessLevel.NONE)
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
