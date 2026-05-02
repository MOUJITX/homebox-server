package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.Good;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query("SELECT i.good.id AS goodId, COUNT(i) AS cnt FROM GoodItem i WHERE i.good.id IN :goodIds GROUP BY i.good.id")
    List<Tuple> countTotalItemsGroupedByGood(@Param("goodIds") List<Long> goodIds);

    @Query("SELECT i.good.id AS goodId, COUNT(i) AS cnt FROM GoodItem i WHERE i.good.id IN :goodIds AND i.inUse = true GROUP BY i.good.id")
    List<Tuple> countInUseItemsGroupedByGood(@Param("goodIds") List<Long> goodIds);
}
