package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.GoodAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoodAttachmentRepository extends JpaRepository<GoodAttachment, Long> {

    List<GoodAttachment> findByGoodId(Long goodId);

    List<GoodAttachment> findByFileIdIn(List<Long> fileIds);

    boolean existsByFileId(Long fileId);
}
