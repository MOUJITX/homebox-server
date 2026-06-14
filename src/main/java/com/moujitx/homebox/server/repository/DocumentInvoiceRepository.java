package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.DocumentInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentInvoiceRepository extends JpaRepository<DocumentInvoice, Long> {

    @Query("SELECT di FROM DocumentInvoice di JOIN FETCH di.invoice WHERE di.document.id = :documentId ORDER BY di.invoice.invoiceDate DESC")
    List<DocumentInvoice> findByDocumentId(@Param("documentId") Long documentId);

    List<DocumentInvoice> findByInvoiceId(Long invoiceId);

    List<DocumentInvoice> findByInvoiceIdIn(List<Long> invoiceIds);

    Optional<DocumentInvoice> findByDocumentIdAndInvoiceId(Long documentId, Long invoiceId);

    void deleteByDocumentIdAndInvoiceId(Long documentId, Long invoiceId);

    boolean existsByDocumentIdAndInvoiceId(Long documentId, Long invoiceId);
}
