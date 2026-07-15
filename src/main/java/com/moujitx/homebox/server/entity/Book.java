package com.moujitx.homebox.server.entity;

import com.moujitx.homebox.server.enums.BookStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String author;

    @Column(unique = true, length = 20)
    private String isbn;

    @Column(name = "custom_barcode", unique = true, nullable = false, length = 100)
    private String customBarcode;

    @Column(nullable = false)
    private boolean serialized = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Book parent;

    @Column(name = "issue_number", length = 50)
    private String issueNumber;

    @Column(length = 255)
    private String publisher;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id")
    private BookCategory category;

    @ManyToOne(optional = false)
    @JoinColumn(name = "location_id")
    private BookLocation location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookStatus status = BookStatus.WANT_TO_READ;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_price", precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 25)
    private List<BookPicture> pictures = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 25)
    private List<BookInvoice> invoiceBindings = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 25)
    private List<BookSeriesMapping> seriesMappings = new ArrayList<>();

    @Setter(AccessLevel.NONE)
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
