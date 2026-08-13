package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.VisitRecord;
import com.moujitx.homebox.server.enums.VisitType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VisitRecordRepository extends JpaRepository<VisitRecord, Long> {

    @Query("SELECT v FROM VisitRecord v JOIN FETCH v.institution")
    Page<VisitRecord> findAllWithInstitution(Pageable pageable);

    @Query("SELECT v FROM VisitRecord v JOIN FETCH v.institution WHERE v.visitType = :visitType")
    Page<VisitRecord> findByVisitType(@Param("visitType") VisitType visitType, Pageable pageable);

    @Query("SELECT v FROM VisitRecord v JOIN FETCH v.institution WHERE v.visitDate BETWEEN :start AND :end")
    Page<VisitRecord> findByVisitDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end, Pageable pageable);

    @Query("SELECT v FROM VisitRecord v JOIN FETCH v.institution WHERE v.institution.id = :institutionId")
    Page<VisitRecord> findByInstitutionId(@Param("institutionId") Long institutionId, Pageable pageable);

    @Query("SELECT v FROM VisitRecord v JOIN FETCH v.institution WHERE v.patientName LIKE %:patientName%")
    Page<VisitRecord> findByPatientNameContaining(@Param("patientName") String patientName, Pageable pageable);

    @Query("SELECT v FROM VisitRecord v JOIN FETCH v.institution WHERE " +
           "(:visitType IS NULL OR v.visitType = :visitType) AND " +
           "(:startDate IS NULL OR v.visitDate >= :startDate) AND " +
           "(:endDate IS NULL OR v.visitDate <= :endDate) AND " +
           "(:institutionId IS NULL OR v.institution.id = :institutionId) AND " +
           "(:patientName IS NULL OR v.patientName LIKE %:patientName%) AND " +
           "(:diagnosis IS NULL OR v.diagnosis LIKE %:diagnosis%)")
    Page<VisitRecord> findWithFilters(@Param("visitType") VisitType visitType,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate,
                                       @Param("institutionId") Long institutionId,
                                       @Param("patientName") String patientName,
                                       @Param("diagnosis") String diagnosis,
                                       Pageable pageable);

    @Query("SELECT DISTINCT v.patientName FROM VisitRecord v ORDER BY v.patientName")
    List<String> findDistinctPatientNames();

    boolean existsByInstitutionId(Long institutionId);
}
