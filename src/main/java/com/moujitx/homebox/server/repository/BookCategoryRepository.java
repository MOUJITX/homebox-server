package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.BookCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {

    Optional<BookCategory> findByName(String name);

    Optional<BookCategory> findByKey(String key);

    boolean existsByName(String name);

    boolean existsByKey(String key);
}
