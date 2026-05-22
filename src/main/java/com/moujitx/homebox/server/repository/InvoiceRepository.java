package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.dto.response.InvoiceResponse;
import com.moujitx.homebox.server.entity.Invoice;
import com.moujitx.homebox.server.enums.InvoiceStatus;
import com.moujitx.homebox.server.enums.InvoiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByFileIdIn(List<Long> fileIds);

    boolean existsByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumberAndIdNot(String invoiceNumber, Long id);

    @Query("SELECT new com.moujitx.homebox.server.dto.response.InvoiceResponse(" +
            "i.id, i.invoiceNumber, i.invoiceDate, i.invoiceType, i.invoiceStatus, " +
            "i.sellerName, i.buyerName, i.amount, i.taxAmount, i.totalAmount, " +
            "(SELECT COUNT(a) FROM InvoiceAttachment a WHERE a.invoice = i), " +
            "i.createdAt, i.updatedAt) " +
            "FROM Invoice i WHERE " +
            "(:search IS NULL OR i.invoiceNumber LIKE %:search% OR i.buyerName LIKE %:search% OR i.sellerName LIKE %:search%) AND " +
            "(:invoiceType IS NULL OR i.invoiceType = :invoiceType) AND " +
            "(:invoiceStatus IS NULL OR i.invoiceStatus = :invoiceStatus) AND " +
            "(:buyerName IS NULL OR i.buyerName = :buyerName) AND " +
            "(:sellerName IS NULL OR i.sellerName = :sellerName)")
    Page<InvoiceResponse> findWithFilters(@Param("search") String search,
                                          @Param("invoiceType") InvoiceType invoiceType,
                                          @Param("invoiceStatus") InvoiceStatus invoiceStatus,
                                          @Param("buyerName") String buyerName,
                                          @Param("sellerName") String sellerName,
                                          Pageable pageable);
}
