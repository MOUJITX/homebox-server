package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.FileRecord;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {

    Page<FileRecord> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<FileRecord> findByIdIn(List<Long> ids);

    @Query("SELECT f FROM FileRecord f WHERE " +
            "(:search IS NULL OR f.originalFilename LIKE %:search%) AND " +
            "(:contentType IS NULL OR f.contentType LIKE CONCAT(:contentType, '%')) AND " +
            "(:status IS NULL OR " +
            "  (:status = 'FAILED' AND (f.extractStatus = com.moujitx.homebox.server.enums.ProcessStatus.FAILED OR f.chunkStatus = com.moujitx.homebox.server.enums.ProcessStatus.FAILED)) OR " +
            "  (:status = 'SUCCESS' AND f.extractStatus = com.moujitx.homebox.server.enums.ProcessStatus.SUCCESS AND f.chunkStatus = com.moujitx.homebox.server.enums.ProcessStatus.SUCCESS) OR " +
            "  (:status = 'PROCESSING' AND f.extractStatus <> com.moujitx.homebox.server.enums.ProcessStatus.FAILED AND f.chunkStatus <> com.moujitx.homebox.server.enums.ProcessStatus.FAILED " +
            "     AND (f.extractStatus <> com.moujitx.homebox.server.enums.ProcessStatus.SUCCESS OR f.chunkStatus <> com.moujitx.homebox.server.enums.ProcessStatus.SUCCESS))) " +
            "ORDER BY f.createdAt DESC")
    Page<FileRecord> findWithFilters(@Param("search") String search,
                                     @Param("contentType") String contentType,
                                     @Param("status") String status,
                                     Pageable pageable);
}
