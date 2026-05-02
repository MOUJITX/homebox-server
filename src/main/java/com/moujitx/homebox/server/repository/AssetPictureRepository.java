package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.AssetPicture;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssetPictureRepository extends JpaRepository<AssetPicture, Long> {

    List<AssetPicture> findByAssetId(Long assetId);

    @Query("SELECT p.asset.id AS assetId, MIN(p.id) AS pictureId FROM AssetPicture p WHERE p.asset.id IN :assetIds GROUP BY p.asset.id")
    List<Tuple> findFirstPictureIdGroupedByAsset(@Param("assetIds") List<Long> assetIds);
}
