package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.BookSeriesMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookSeriesMappingRepository extends JpaRepository<BookSeriesMapping, Long> {

    @Query("SELECT bsm FROM BookSeriesMapping bsm JOIN FETCH bsm.series WHERE bsm.book.id = :bookId")
    List<BookSeriesMapping> findByBookId(@Param("bookId") Long bookId);

    boolean existsByBookIdAndSeriesId(Long bookId, Long seriesId);

    void deleteByBookIdAndSeriesId(Long bookId, Long seriesId);

    void deleteByBookId(Long bookId);
}
