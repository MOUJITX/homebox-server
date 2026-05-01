package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.AssetPlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetPlaceRepository extends JpaRepository<AssetPlace, Long> {

    Optional<AssetPlace> findByName(String name);

    boolean existsByName(String name);
}
