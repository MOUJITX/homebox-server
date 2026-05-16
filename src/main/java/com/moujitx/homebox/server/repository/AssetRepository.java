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

    @Query("SELECT a FROM Asset a WHERE " +
            "(:parentOnly = false OR a.parent IS NULL) AND " +
            "(:search IS NULL OR a.name LIKE %:search% OR a.barcode LIKE %:search% OR a.serialNumber LIKE %:search%) AND " +
            "(:categoryId IS NULL OR a.category.id = :categoryId) AND " +
            "(:placeId IS NULL OR a.place.id = :placeId) AND " +
            "(:isInUse IS NULL OR a.inUse = :isInUse)")
    Page<Asset> findWithFilters(@Param("search") String search,
                                @Param("categoryId") Long categoryId,
                                @Param("placeId") Long placeId,
                                @Param("isInUse") Boolean isInUse,
                                @Param("parentOnly") Boolean parentOnly,
                                Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE " +
            "(:parentOnly = false OR a.parent IS NULL) AND " +
            "(:search IS NULL OR a.name LIKE %:search% OR a.barcode LIKE %:search% OR a.serialNumber LIKE %:search%) AND " +
            "(:categoryId IS NULL OR a.category.id = :categoryId) AND " +
            "(:placeId IS NULL OR a.place.id = :placeId) AND " +
            "(:isInUse IS NULL OR a.inUse = :isInUse) AND " +
            "(:warrantyStatus IS NULL OR " +
            "  (:warrantyStatus = 'OUT_WARRANTY' AND a.hasWarranty = true AND a.expirationDate IS NOT NULL AND a.expirationDate < CURRENT_DATE) OR " +
            "  (:warrantyStatus = 'IN_WARRANTY'  AND a.hasWarranty = true AND a.expirationDate IS NOT NULL AND a.expirationDate >= CURRENT_DATE) OR " +
            "  (:warrantyStatus = 'NO_WARRANTY'  AND (a.hasWarranty = false OR (a.hasWarranty = true AND a.expirationDate IS NULL))) " +
            ")")
    Page<Asset> findWithFilters(@Param("search") String search,
                                @Param("categoryId") Long categoryId,
                                @Param("placeId") Long placeId,
                                @Param("isInUse") Boolean isInUse,
                                @Param("warrantyStatus") String warrantyStatus,
                                @Param("parentOnly") Boolean parentOnly,
                                Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.parent.id = :parentId ORDER BY a.shopDate DESC")
    List<Asset> findByParentId(@Param("parentId") Long parentId);

    boolean existsByBarcodeAndSerialNumber(String barcode, String serialNumber);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByPlaceId(Long placeId);

    boolean existsByStoreId(Long storeId);

    boolean existsByParentId(Long parentId);

    @Query("SELECT a.parent.id AS parentId, COUNT(a) AS cnt FROM Asset a WHERE a.parent.id IN :parentIds GROUP BY a.parent.id")
    List<Tuple> countSubAssetsGroupedByParent(@Param("parentIds") List<Long> parentIds);

    @Query("SELECT a.parent.id AS parentId, COALESCE(SUM(a.price), 0) AS priceSum FROM Asset a WHERE a.parent.id IN :parentIds GROUP BY a.parent.id")
    List<Tuple> sumSubAssetPricesGroupedByParent(@Param("parentIds") List<Long> parentIds);

    @Query("SELECT a FROM Asset a JOIN FETCH a.category JOIN FETCH a.place " +
            "WHERE a.inUse = true AND a.hasWarranty = true AND a.expirationDate IS NOT NULL AND a.expirationDate >= CURRENT_DATE " +
            "ORDER BY a.expirationDate ASC")
    List<Asset> findWarrantyExpiringAssets(Pageable pageable);

    @Query("SELECT a FROM Asset a JOIN FETCH a.category JOIN FETCH a.place " +
            "WHERE a.inUse = true " +
            "ORDER BY a.shopDate DESC")
    List<Asset> findInUseAssetsOrderByShopDateDesc(Pageable pageable);

    @Query("SELECT COALESCE(SUM(a.price), 0) FROM Asset a")
    java.math.BigDecimal sumAllPrices();
}
