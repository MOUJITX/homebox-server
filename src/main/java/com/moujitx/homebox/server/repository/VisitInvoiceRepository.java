package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.VisitInvoice;
import com.moujitx.homebox.server.enums.VisitSourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitInvoiceRepository extends JpaRepository<VisitInvoice, Long> {

    List<VisitInvoice> findByVisitId(Long visitId);

    List<VisitInvoice> findByVisitIdAndSourceType(Long visitId, VisitSourceType sourceType);

    List<VisitInvoice> findByInvoiceId(Long invoiceId);

    List<VisitInvoice> findByInvoiceIdIn(List<Long> invoiceIds);

    void deleteByVisitIdAndSourceTypeAndSourceId(Long visitId, VisitSourceType sourceType, Long sourceId);
}
