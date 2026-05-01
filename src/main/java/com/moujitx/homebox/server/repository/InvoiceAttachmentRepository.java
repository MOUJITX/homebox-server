package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.InvoiceAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceAttachmentRepository extends JpaRepository<InvoiceAttachment, Long> {

    List<InvoiceAttachment> findByInvoiceId(Long invoiceId);
}
