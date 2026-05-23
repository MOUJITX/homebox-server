package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.VisitInvoice;
import com.moujitx.homebox.server.enums.VisitSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface VisitInvoiceRepository extends JpaRepository<VisitInvoice, Long> {

    List<VisitInvoice> findByVisitId(Long visitId);

    List<VisitInvoice> findByVisitIdAndSourceType(Long visitId, VisitSourceType sourceType);

    List<VisitInvoice> findByInvoiceId(Long invoiceId);

    List<VisitInvoice> findByInvoiceIdIn(List<Long> invoiceIds);

    @Modifying
    @Transactional
    void deleteByVisitIdAndSourceTypeAndSourceId(Long visitId, VisitSourceType sourceType, Long sourceId);
}
