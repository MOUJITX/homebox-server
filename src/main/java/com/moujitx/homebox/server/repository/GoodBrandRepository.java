package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.GoodBrand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoodBrandRepository extends JpaRepository<GoodBrand, Long> {

    Optional<GoodBrand> findByBrandName(String brandName);

    boolean existsByBrandName(String brandName);
}
