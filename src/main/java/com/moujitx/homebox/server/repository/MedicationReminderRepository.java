package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.MedicationReminder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MedicationReminderRepository extends JpaRepository<MedicationReminder, Long> {

    boolean existsByGoodIdAndCourseStartDateAndCourseEndDate(Long goodId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT r FROM MedicationReminder r JOIN FETCH r.good g JOIN FETCH g.category JOIN FETCH g.brand WHERE r.enabled = :enabled")
    Page<MedicationReminder> findByEnabled(@Param("enabled") boolean enabled, Pageable pageable);

    @Query("SELECT r FROM MedicationReminder r JOIN FETCH r.good g JOIN FETCH g.category JOIN FETCH g.brand")
    Page<MedicationReminder> findAllWithGood(Pageable pageable);

    @Query("SELECT r FROM MedicationReminder r JOIN FETCH r.good g JOIN FETCH g.category JOIN FETCH g.brand WHERE r.enabled = true")
    List<MedicationReminder> findAllEnabledWithGood();
}
