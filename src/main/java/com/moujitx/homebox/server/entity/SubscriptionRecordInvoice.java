package com.moujitx.homebox.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_record_invoices", uniqueConstraints = @UniqueConstraint(columnNames = {"record_id", "invoice_id"}))
@Getter
@Setter
@NoArgsConstructor
public class SubscriptionRecordInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "record_id")
    private SubscriptionRecord record;

    @ManyToOne(optional = false)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
