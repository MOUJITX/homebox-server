package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.BookInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookInvoiceRepository extends JpaRepository<BookInvoice, Long> {

    @Query("SELECT bi FROM BookInvoice bi JOIN FETCH bi.invoice WHERE bi.book.id = :bookId ORDER BY bi.invoice.invoiceDate DESC")
    List<BookInvoice> findByBookId(@Param("bookId") Long bookId);

    boolean existsByBookIdAndInvoiceId(Long bookId, Long invoiceId);

    void deleteByBookIdAndInvoiceId(Long bookId, Long invoiceId);
}
