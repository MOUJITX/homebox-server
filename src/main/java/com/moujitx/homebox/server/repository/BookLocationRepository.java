package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.BookLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookLocationRepository extends JpaRepository<BookLocation, Long> {

    Optional<BookLocation> findByName(String name);

    boolean existsByName(String name);
}
