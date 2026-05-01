package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByName(String name);

    boolean existsByName(String name);
}
