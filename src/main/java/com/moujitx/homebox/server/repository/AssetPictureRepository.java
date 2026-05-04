package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.AssetPicture;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssetPictureRepository extends JpaRepository<AssetPicture, Long> {

    List<AssetPicture> findByAssetId(Long assetId);

    @Query("SELECT p.asset.id AS assetId, p.file.storedFilename AS storedFilename FROM AssetPicture p WHERE p.id IN (SELECT MIN(p2.id) FROM AssetPicture p2 WHERE p2.asset.id IN :assetIds GROUP BY p2.asset.id)")
    List<Tuple> findFirstPictureIdGroupedByAsset(@Param("assetIds") List<Long> assetIds);
}
