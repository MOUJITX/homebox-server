package com.moujitx.homebox.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_series_mapping", uniqueConstraints = @UniqueConstraint(columnNames = {"book_id", "series_id"}))
@Getter
@Setter
@NoArgsConstructor
public class BookSeriesMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(optional = false)
    @JoinColumn(name = "series_id")
    private BookSeries series;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
