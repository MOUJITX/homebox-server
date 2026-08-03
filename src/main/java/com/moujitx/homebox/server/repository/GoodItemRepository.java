package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.GoodItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoodItemRepository extends JpaRepository<GoodItem, Long> {

    @Query("SELECT gi FROM GoodItem gi WHERE gi.good.id = :goodId ORDER BY gi.productDate DESC")
    List<GoodItem> findByGoodId(@Param("goodId") Long goodId);

    boolean existsByGoodId(Long goodId);

    @Query("SELECT gi FROM GoodItem gi JOIN FETCH gi.good g JOIN FETCH g.category JOIN FETCH g.brand " +
            "WHERE gi.inUse = true AND gi.expirationDate >= CURRENT_DATE " +
            "ORDER BY gi.expirationDate ASC")
    List<GoodItem> findInUseItemsOrderByExpirationAsc(Pageable pageable);

    @Query("SELECT gi FROM GoodItem gi JOIN FETCH gi.good g JOIN FETCH g.category JOIN FETCH g.brand " +
            "WHERE gi.inUse = true " +
            "ORDER BY gi.expirationDate ASC")
    List<GoodItem> findAllInUseWithGood();

    @Query("SELECT gi FROM GoodItem gi JOIN FETCH gi.good g JOIN FETCH g.category JOIN FETCH g.brand " +
            "WHERE gi.inUse = true " +
            "ORDER BY gi.createdAt DESC")
    List<GoodItem> findInUseItemsOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT COUNT(gi) FROM GoodItem gi WHERE gi.inUse = true")
    long countInUseItems();

    /**
     * 批量查询指定 goodId 列表下的 items，按过期日期升序排序
     * 用于列表页面显示每个商品的前 N 个物品
     */
    @Query("SELECT gi FROM GoodItem gi WHERE gi.good.id IN :goodIds ORDER BY gi.good.id, gi.inUse DESC, gi.expirationDate ASC")
    List<GoodItem> findByGoodIdIn(@Param("goodIds") List<Long> goodIds);
}
