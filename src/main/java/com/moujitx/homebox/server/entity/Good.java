package com.moujitx.homebox.server.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "goods")
@Getter
@Setter
@NoArgsConstructor
public class Good {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productName;

    @Column(unique = true, nullable = false)
    private String barcode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id")
    private GoodCategory category;

    @ManyToOne(optional = false)
    @JoinColumn(name = "brand_id")
    private GoodBrand brand;

    @Column(nullable = false)
    private int expiringSoonDays = 30;

    @OneToMany(mappedBy = "good", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 25)
    private List<GoodItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "good", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 25)
    private List<GoodPicture> pictures = new ArrayList<>();

    @OneToMany(mappedBy = "good", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 25)
    private List<GoodAttachment> attachments = new ArrayList<>();

    @Setter(AccessLevel.NONE)
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
