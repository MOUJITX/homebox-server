package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.DocumentAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentAttachmentRepository extends JpaRepository<DocumentAttachment, Long> {

    List<DocumentAttachment> findByDocumentId(Long documentId);

    List<DocumentAttachment> findByFileIdIn(List<Long> fileIds);

    boolean existsByFileId(Long fileId);
}
