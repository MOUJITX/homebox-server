package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.GoodItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GoodItemRepository extends JpaRepository<GoodItem, Long> {

    List<GoodItem> findByGoodId(Long goodId);

    boolean existsByGoodId(Long goodId);

    @Query("SELECT gi FROM GoodItem gi JOIN FETCH gi.good g JOIN FETCH g.category JOIN FETCH g.brand " +
            "WHERE gi.inUse = true AND gi.expirationDate >= CURRENT_DATE " +
            "ORDER BY gi.expirationDate ASC")
    List<GoodItem> findInUseItemsOrderByExpirationAsc(Pageable pageable);

    @Query("SELECT gi FROM GoodItem gi JOIN FETCH gi.good g JOIN FETCH g.category JOIN FETCH g.brand " +
            "WHERE gi.inUse = true " +
            "ORDER BY gi.createdAt DESC")
    List<GoodItem> findInUseItemsOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT COUNT(gi) FROM GoodItem gi WHERE gi.inUse = true")
    long countInUseItems();
}
