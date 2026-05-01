package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.AssetStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetStoreRepository extends JpaRepository<AssetStore, Long> {

    Optional<AssetStore> findByName(String name);

    boolean existsByName(String name);
}
