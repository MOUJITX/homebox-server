package com.moujitx.homebox.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "asset_attachments")
@Getter
@Setter
@NoArgsConstructor
public class AssetAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private FileRecord file;
}
