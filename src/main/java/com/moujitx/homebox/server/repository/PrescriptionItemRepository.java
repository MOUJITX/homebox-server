package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {

    List<PrescriptionItem> findByPrescriptionId(Long prescriptionId);

    long countByPrescriptionId(Long prescriptionId);
}
