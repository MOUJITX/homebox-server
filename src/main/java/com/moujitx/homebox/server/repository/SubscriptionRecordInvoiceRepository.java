package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.SubscriptionRecordInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRecordInvoiceRepository extends JpaRepository<SubscriptionRecordInvoice, Long> {

    @Query("SELECT sri FROM SubscriptionRecordInvoice sri JOIN FETCH sri.invoice WHERE sri.record.id = :recordId ORDER BY sri.invoice.invoiceDate DESC")
    List<SubscriptionRecordInvoice> findByRecordId(@Param("recordId") Long recordId);

    List<SubscriptionRecordInvoice> findByInvoiceId(Long invoiceId);

    List<SubscriptionRecordInvoice> findByInvoiceIdIn(List<Long> invoiceIds);

    Optional<SubscriptionRecordInvoice> findByRecordIdAndInvoiceId(Long recordId, Long invoiceId);

    void deleteByRecordIdAndInvoiceId(Long recordId, Long invoiceId);

    boolean existsByRecordIdAndInvoiceId(Long recordId, Long invoiceId);
}
