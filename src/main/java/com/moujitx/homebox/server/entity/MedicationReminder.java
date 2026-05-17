package com.moujitx.homebox.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medication_reminders", uniqueConstraints = {
        @UniqueConstraint(name = "uk_medication_course", columnNames = {"good_id", "course_start_date", "course_end_date"})
})
@Getter
@Setter
@NoArgsConstructor
public class MedicationReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "good_id", nullable = false)
    private Good good;

    @Column(name = "dosage_method", length = 50)
    private String dosageMethod;

    @Column(name = "dosage_quantity", length = 50)
    private String dosageQuantity;

    @Column(name = "dosage_unit", length = 50)
    private String dosageUnit;

    @Column(name = "dosage_note", length = 255)
    private String dosageNote;

    @Column(name = "frequency_hours", length = 100, nullable = false)
    private String frequencyHours;

    @Column(name = "course_start_date", nullable = false)
    private LocalDate courseStartDate;

    @Column(name = "course_end_date", nullable = false)
    private LocalDate courseEndDate;

    @Column(nullable = false)
    private boolean enabled = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
