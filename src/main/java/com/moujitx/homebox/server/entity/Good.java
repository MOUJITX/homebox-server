package com.moujitx.homebox.server.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "goods")
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
    private List<GoodItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "good", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoodPicture> pictures = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Good() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public GoodCategory getCategory() {
        return category;
    }

    public void setCategory(GoodCategory category) {
        this.category = category;
    }

    public GoodBrand getBrand() {
        return brand;
    }

    public void setBrand(GoodBrand brand) {
        this.brand = brand;
    }

    public int getExpiringSoonDays() {
        return expiringSoonDays;
    }

    public void setExpiringSoonDays(int expiringSoonDays) {
        this.expiringSoonDays = expiringSoonDays;
    }

    public List<GoodItem> getItems() {
        return items;
    }

    public void setItems(List<GoodItem> items) {
        this.items = items;
    }

    public List<GoodPicture> getPictures() {
        return pictures;
    }

    public void setPictures(List<GoodPicture> pictures) {
        this.pictures = pictures;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
