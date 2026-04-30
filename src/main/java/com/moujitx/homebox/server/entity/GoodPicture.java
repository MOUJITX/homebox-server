package com.moujitx.homebox.server.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "good_pictures")
public class GoodPicture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "good_id")
    private Good good;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "file_id")
    private FileRecord file;

    public GoodPicture() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Good getGood() {
        return good;
    }

    public void setGood(Good good) {
        this.good = good;
    }

    public FileRecord getFile() {
        return file;
    }

    public void setFile(FileRecord file) {
        this.file = file;
    }
}
