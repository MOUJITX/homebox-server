package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.Good;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GoodRepository extends JpaRepository<Good, Long> {

    Optional<Good> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByBrandId(Long brandId);

    @Query("SELECT g FROM Good g WHERE " +
            "(:search IS NULL OR g.productName LIKE %:search% OR g.barcode LIKE %:search%) AND " +
            "(:categoryId IS NULL OR g.category.id = :categoryId) AND " +
            "(:brandId IS NULL OR g.brand.id = :brandId)")
    Page<Good> findWithFilters(@Param("search") String search,
                               @Param("categoryId") Long categoryId,
                               @Param("brandId") Long brandId,
                               Pageable pageable);
}
