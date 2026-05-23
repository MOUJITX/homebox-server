package com.moujitx.homebox.server.entity;

import com.moujitx.homebox.server.enums.Gender;
import com.moujitx.homebox.server.enums.VisitType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "visit_records")
@Getter
@Setter
@NoArgsConstructor
public class VisitRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String patientName;

    @Column
    private Integer patientAge;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender patientGender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private VisitType visitType;

    @Column(nullable = false)
    private LocalDate visitDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private MedicalInstitution institution;

    @Column(name = "medical_content", columnDefinition = "LONGTEXT")
    private String medicalContent;

    @Column(length = 50)
    private String doctor;

    @Column(length = 50)
    private String department;

    @Column
    private LocalDate dischargeDate;

    @Column(length = 50)
    private String dischargeDept;

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VisitAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VisitInvoice> invoices = new ArrayList<>();

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VisitExamination> examinations = new ArrayList<>();

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VisitLabTest> labTests = new ArrayList<>();

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VisitPrescription> prescriptions = new ArrayList<>();

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
