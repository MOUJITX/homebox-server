package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.VisitLabTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitLabTestRepository extends JpaRepository<VisitLabTest, Long> {

    List<VisitLabTest> findByVisitId(Long visitId);

    Page<VisitLabTest> findByVisitId(Long visitId, Pageable pageable);

    long countByVisitId(Long visitId);
}
