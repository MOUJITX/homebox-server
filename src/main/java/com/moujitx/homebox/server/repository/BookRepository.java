package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.Book;
import com.moujitx.homebox.server.enums.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.category LEFT JOIN FETCH b.location " +
           "WHERE b.parent IS NULL " +
           "AND (:search IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(b.customBarcode) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:categoryId IS NULL OR b.category.id = :categoryId) " +
           "AND (:locationId IS NULL OR b.location.id = :locationId) " +
           "AND (:status IS NULL OR b.status = :status)")
    Page<Book> findTopLevelBooks(@Param("search") String search,
                                  @Param("categoryId") Long categoryId,
                                  @Param("locationId") Long locationId,
                                  @Param("status") BookStatus status,
                                  Pageable pageable);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.category LEFT JOIN FETCH b.location WHERE b.id = :id")
    Optional<Book> findByIdWithDetails(@Param("id") Long id);

    List<Book> findByParentIdOrderByIssueNumberDesc(Long parentId);

    long countByParentId(Long parentId);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(b.customBarcode, LENGTH(:prefix) + 1) AS LONG)), 0) " +
           "FROM Book b WHERE b.customBarcode LIKE CONCAT(:prefix, '%') AND b.parent IS NULL")
    long findMaxSequenceForPrefix(@Param("prefix") String prefix);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByLocationId(Long locationId);

    boolean existsByCustomBarcode(String customBarcode);

    long countByIsbn(String isbn);

    List<Book> findByIsbn(String isbn);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.category LEFT JOIN FETCH b.location " +
           "LEFT JOIN FETCH b.pictures p LEFT JOIN FETCH p.file " +
           "WHERE b.id = :id")
    Optional<Book> findByIdWithAllDetails(@Param("id") Long id);
}
