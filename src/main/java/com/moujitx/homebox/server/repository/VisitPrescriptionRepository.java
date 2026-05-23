package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.VisitPrescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitPrescriptionRepository extends JpaRepository<VisitPrescription, Long> {

    List<VisitPrescription> findByVisitId(Long visitId);

    Page<VisitPrescription> findByVisitId(Long visitId, Pageable pageable);

    long countByVisitId(Long visitId);
}
