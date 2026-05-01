package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.AssetPicture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetPictureRepository extends JpaRepository<AssetPicture, Long> {

    List<AssetPicture> findByAssetId(Long assetId);
}
