package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.FileRecord;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {

    Page<FileRecord> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<FileRecord> findByIdIn(List<Long> ids);
}
