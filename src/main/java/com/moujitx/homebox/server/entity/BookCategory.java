package com.moujitx.homebox.server.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_categories")
@Getter
@Setter
@NoArgsConstructor
public class BookCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "category_key", unique = true, nullable = false, length = 10)
    private String key;

    @Column(nullable = false)
    private boolean serialized = false;

    private String description;

    @Setter(AccessLevel.NONE)
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public BookCategory(String name, String key, boolean serialized, String description) {
        this.name = name;
        this.key = key;
        this.serialized = serialized;
        this.description = description;
    }
}
