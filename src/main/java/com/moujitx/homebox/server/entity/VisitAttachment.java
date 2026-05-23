package com.moujitx.homebox.server.entity;

import com.moujitx.homebox.server.enums.VisitSourceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "visit_attachments")
@Getter
@Setter
@NoArgsConstructor
public class VisitAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private VisitRecord visit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileRecord file;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private VisitSourceType sourceType;

    @Column(nullable = false)
    private Long sourceId;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
