package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.Asset;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    @Query("SELECT a FROM Asset a WHERE a.parent IS NULL AND " +
            "(:search IS NULL OR a.name LIKE %:search% OR a.barcode LIKE %:search% OR a.serialNumber LIKE %:search%) AND " +
            "(:categoryId IS NULL OR a.category.id = :categoryId) AND " +
            "(:placeId IS NULL OR a.place.id = :placeId) AND " +
            "(:isInUse IS NULL OR a.inUse = :isInUse)")
    Page<Asset> findWithFilters(@Param("search") String search,
                                @Param("categoryId") Long categoryId,
                                @Param("placeId") Long placeId,
                                @Param("isInUse") Boolean isInUse,
                                Pageable pageable);

    List<Asset> findByParentId(Long parentId);

    boolean existsByBarcodeAndSerialNumber(String barcode, String serialNumber);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByPlaceId(Long placeId);

    boolean existsByStoreId(Long storeId);

    boolean existsByParentId(Long parentId);

    @Query("SELECT a.parent.id AS parentId, COUNT(a) AS cnt FROM Asset a WHERE a.parent.id IN :parentIds GROUP BY a.parent.id")
    List<Tuple> countSubAssetsGroupedByParent(@Param("parentIds") List<Long> parentIds);
}
