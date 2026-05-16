package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.AssetInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssetInvoiceRepository extends JpaRepository<AssetInvoice, Long> {

    @Query("SELECT ai FROM AssetInvoice ai JOIN FETCH ai.invoice WHERE ai.asset.id = :assetId ORDER BY ai.invoice.invoiceDate DESC")
    List<AssetInvoice> findByAssetId(@Param("assetId") Long assetId);

    List<AssetInvoice> findByInvoiceId(Long invoiceId);

    List<AssetInvoice> findByInvoiceIdIn(List<Long> invoiceIds);

    Optional<AssetInvoice> findByAssetIdAndInvoiceId(Long assetId, Long invoiceId);

    void deleteByAssetIdAndInvoiceId(Long assetId, Long invoiceId);

    boolean existsByAssetIdAndInvoiceId(Long assetId, Long invoiceId);

    @Query("SELECT DISTINCT ai.asset.id FROM AssetInvoice ai WHERE ai.asset.id IN :assetIds")
    List<Long> findAssetIdsWithInvoices(@Param("assetIds") List<Long> assetIds);
}
