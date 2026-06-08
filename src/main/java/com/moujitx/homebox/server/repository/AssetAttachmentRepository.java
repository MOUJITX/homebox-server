package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.AssetAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetAttachmentRepository extends JpaRepository<AssetAttachment, Long> {

    List<AssetAttachment> findByAssetId(Long assetId);

    List<AssetAttachment> findByFileIdIn(List<Long> fileIds);

    boolean existsByFileId(Long fileId);
}
