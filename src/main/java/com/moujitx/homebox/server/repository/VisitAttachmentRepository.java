package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.VisitAttachment;
import com.moujitx.homebox.server.enums.VisitSourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitAttachmentRepository extends JpaRepository<VisitAttachment, Long> {

    List<VisitAttachment> findByVisitId(Long visitId);

    List<VisitAttachment> findByVisitIdAndSourceType(Long visitId, VisitSourceType sourceType);

    List<VisitAttachment> findByFileId(Long fileId);

    void deleteByVisitIdAndSourceTypeAndSourceId(Long visitId, VisitSourceType sourceType, Long sourceId);
}
