package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.BookSeries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookSeriesRepository extends JpaRepository<BookSeries, Long> {

    Optional<BookSeries> findByName(String name);

    boolean existsByName(String name);
}
