package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, Long> {

    Optional<DocumentCategory> findByName(String name);

    boolean existsByName(String name);
}
