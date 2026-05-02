package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.AssetInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetInvoiceRepository extends JpaRepository<AssetInvoice, Long> {

    List<AssetInvoice> findByAssetId(Long assetId);

    List<AssetInvoice> findByInvoiceId(Long invoiceId);

    Optional<AssetInvoice> findByAssetIdAndInvoiceId(Long assetId, Long invoiceId);

    void deleteByAssetIdAndInvoiceId(Long assetId, Long invoiceId);

    boolean existsByAssetIdAndInvoiceId(Long assetId, Long invoiceId);
}
