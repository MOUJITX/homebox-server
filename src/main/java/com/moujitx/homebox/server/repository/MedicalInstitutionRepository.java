package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.MedicalInstitution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalInstitutionRepository extends JpaRepository<MedicalInstitution, Long> {

    boolean existsByName(String name);

    Page<MedicalInstitution> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
