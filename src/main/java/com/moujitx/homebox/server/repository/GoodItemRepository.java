package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.GoodItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoodItemRepository extends JpaRepository<GoodItem, Long> {

    List<GoodItem> findByGoodId(Long goodId);

    boolean existsByGoodId(Long goodId);
}
