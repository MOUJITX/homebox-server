package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.Invoice;
import com.moujitx.homebox.server.enums.InvoiceStatus;
import com.moujitx.homebox.server.enums.InvoiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    boolean existsByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumberAndIdNot(String invoiceNumber, Long id);

    @Query("SELECT i FROM Invoice i WHERE " +
            "(:search IS NULL OR i.invoiceNumber LIKE %:search% OR i.buyerName LIKE %:search% OR i.sellerName LIKE %:search%) AND " +
            "(:invoiceType IS NULL OR i.invoiceType = :invoiceType) AND " +
            "(:invoiceStatus IS NULL OR i.invoiceStatus = :invoiceStatus) AND " +
            "(:buyerName IS NULL OR i.buyerName = :buyerName) AND " +
            "(:sellerName IS NULL OR i.sellerName = :sellerName)")
    Page<Invoice> findWithFilters(@Param("search") String search,
                                  @Param("invoiceType") InvoiceType invoiceType,
                                  @Param("invoiceStatus") InvoiceStatus invoiceStatus,
                                  @Param("buyerName") String buyerName,
                                  @Param("sellerName") String sellerName,
                                  Pageable pageable);
}
