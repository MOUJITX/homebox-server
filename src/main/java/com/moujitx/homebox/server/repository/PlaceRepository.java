package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByName(String name);

    boolean existsByName(String name);
}
