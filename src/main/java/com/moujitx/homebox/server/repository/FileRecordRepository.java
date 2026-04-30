package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
}
