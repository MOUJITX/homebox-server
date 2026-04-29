package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.GoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoodCategoryRepository extends JpaRepository<GoodCategory, Long> {

    Optional<GoodCategory> findByName(String name);

    boolean existsByName(String name);
}
