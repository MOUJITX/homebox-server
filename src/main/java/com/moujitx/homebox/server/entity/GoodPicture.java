package com.moujitx.homebox.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "good_pictures")
@Getter
@Setter
@NoArgsConstructor
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
}
