package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.VisitExamination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitExaminationRepository extends JpaRepository<VisitExamination, Long> {

    List<VisitExamination> findByVisitId(Long visitId);

    Page<VisitExamination> findByVisitId(Long visitId, Pageable pageable);

    long countByVisitId(Long visitId);
}
