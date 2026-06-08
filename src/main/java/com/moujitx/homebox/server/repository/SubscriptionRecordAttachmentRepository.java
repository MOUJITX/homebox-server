package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.SubscriptionRecordAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRecordAttachmentRepository extends JpaRepository<SubscriptionRecordAttachment, Long> {

    List<SubscriptionRecordAttachment> findByRecordId(Long recordId);

    List<SubscriptionRecordAttachment> findByFileIdIn(List<Long> fileIds);

    boolean existsByFileId(Long fileId);
}
