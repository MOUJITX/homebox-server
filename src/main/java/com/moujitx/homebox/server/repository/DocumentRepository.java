package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.Document;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("SELECT d FROM Document d WHERE " +
            "(:parentOnly = false OR d.parent IS NULL) AND " +
            "(:search IS NULL OR d.name LIKE %:search% OR d.holder LIKE %:search% OR d.documentNumber LIKE %:search%) AND " +
            "(:categoryId IS NULL OR d.category.id = :categoryId) AND " +
            "(:status IS NULL OR d.status = :status) AND " +
            "(:importance IS NULL OR d.importance = :importance)")
    Page<Document> findWithFilters(@Param("search") String search,
                                   @Param("categoryId") Long categoryId,
                                   @Param("status") String status,
                                   @Param("importance") String importance,
                                   @Param("parentOnly") Boolean parentOnly,
                                   Pageable pageable);

    List<Document> findByParentIdOrderByCreatedAtDesc(Long parentId);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByParentId(Long parentId);

    @Query("SELECT d.parent.id AS parentId, COUNT(d) AS cnt FROM Document d WHERE d.parent.id IN :parentIds GROUP BY d.parent.id")
    List<Tuple> countSubDocumentsGroupedByParent(@Param("parentIds") List<Long> parentIds);

    @Query("SELECT d FROM Document d WHERE d.parent IS NULL AND d.status = 'ACTIVE' " +
            "AND d.expiryDate IS NOT NULL AND d.expiryDate < :date ORDER BY d.expiryDate ASC")
    List<Document> findExpiringSoon(@Param("date") LocalDate date, Pageable pageable);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.parent IS NULL AND d.status = 'ACTIVE'")
    long countActiveTopLevel();
}
