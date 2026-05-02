package com.moujitx.homebox.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "asset_invoices", uniqueConstraints = @UniqueConstraint(columnNames = {"asset_id", "invoice_id"}))
@Getter
@Setter
@NoArgsConstructor
public class AssetInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(optional = false)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
