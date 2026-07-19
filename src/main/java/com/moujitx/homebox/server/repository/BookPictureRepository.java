package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.BookPicture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookPictureRepository extends JpaRepository<BookPicture, Long> {

    @Query("SELECT bp FROM BookPicture bp JOIN FETCH bp.file WHERE bp.book.id = :bookId ORDER BY bp.id ASC")
    List<BookPicture> findByBookId(@Param("bookId") Long bookId);

    void deleteByBookIdAndFileId(Long bookId, Long fileId);
}
