package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateAssetRequest;
import com.moujitx.homebox.server.dto.request.UpdateAssetRequest;
import com.moujitx.homebox.server.dto.response.AssetAttachmentResponse;
import com.moujitx.homebox.server.dto.response.AssetDetailResponse;
import com.moujitx.homebox.server.dto.response.AssetInvoiceResponse;
import com.moujitx.homebox.server.dto.response.AssetResponse;
import com.moujitx.homebox.server.entity.*;
import com.moujitx.homebox.server.enums.WarrantyStatus;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.*;
import com.moujitx.homebox.server.util.DateCalculator;
import com.moujitx.homebox.server.util.OssUrlBuilder;
import com.moujitx.homebox.server.util.StringUtil;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetCategoryRepository categoryRepository;
    private final AssetPlaceRepository placeRepository;
    private final AssetStoreRepository storeRepository;
    private final AssetPictureRepository assetPictureRepository;
    private final AssetAttachmentRepository assetAttachmentRepository;
    private final AssetInvoiceRepository assetInvoiceRepository;
    private final InvoiceRepository invoiceRepository;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public Page<AssetResponse> getAssets(String search, Long categoryId, Long placeId, Boolean isInUse,
                                          WarrantyStatus warrantyStatus, Boolean parentOnly, Pageable pageable) {
        String searchParam = StringUtil.normalizeSearch(search);

        String warrantyStatusName = warrantyStatus != null ? warrantyStatus.name() : null;

        Page<Asset> assetsPage = warrantyStatusName != null
                ? assetRepository.findWithFilters(searchParam, categoryId, placeId, isInUse, warrantyStatusName, parentOnly, pageable)
                : assetRepository.findWithFilters(searchParam, categoryId, placeId, isInUse, parentOnly, pageable);

        List<Asset> assets = assetsPage.getContent();
        Map<Long, Integer> subAssetCounts = loadSubAssetCounts(assets);
        Map<Long, String> firstPictureUrls = loadFirstPictureUrls(assets);
        Map<Long, BigDecimal> subAssetPriceSums = loadSubAssetPriceSums(assets);
        Set<Long> assetIdsWithInvoices = loadAssetIdsWithInvoices(assets);

        List<AssetResponse> responses = assets.stream()
                .map(asset -> {
                    AssetResponse r = AssetResponse.from(asset, computeWarrantyStatus(asset),
                            subAssetCounts, firstPictureUrls, subAssetPriceSums);
                    r.setHasInvoice(assetIdsWithInvoices.contains(asset.getId()));
                    return r;
                })
                .toList();

        List<Long> parentIds = responses.stream()
                .filter(r -> r.getSubAssetCount() > 0)
                .map(AssetResponse::getId)
                .toList();
        if (!parentIds.isEmpty()) {
            List<Asset> subAssetEntities = assetRepository.findByParentIdIn(parentIds);
            Map<Long, List<AssetResponse>> subAssetMap = subAssetEntities.stream()
                    .collect(Collectors.groupingBy(
                            sa -> sa.getParent().getId(),
                            Collectors.mapping(
                                    sa -> AssetResponse.from(sa, computeWarrantyStatus(sa), 0),
                                    Collectors.toList())));
            responses.forEach(r -> {
                List<AssetResponse> subs = subAssetMap.get(r.getId());
                if (subs != null) {
                    r.setSubAssets(subs);
                }
            });
        }

        return new PageImpl<>(responses, pageable, assetsPage.getTotalElements());
    }

    private Set<Long> loadAssetIdsWithInvoices(List<Asset> assets) {
        List<Long> ids = assets.stream().map(Asset::getId).toList();
        if (ids.isEmpty()) return Set.of();
        return new HashSet<>(assetInvoiceRepository.findAssetIdsWithInvoices(ids));
    }

    private Map<Long, Integer> loadSubAssetCounts(List<Asset> assets) {
        List<Long> ids = assets.stream().map(Asset::getId).toList();
        if (ids.isEmpty()) return Map.of();
        return assetRepository.countSubAssetsGroupedByParent(ids).stream()
                .collect(Collectors.toMap(t -> (Long) t.get(0), t -> ((Number) t.get(1)).intValue(), (a, b) -> b));
    }

    private Map<Long, BigDecimal> loadSubAssetPriceSums(List<Asset> assets) {
        List<Long> ids = assets.stream().map(Asset::getId).toList();
        if (ids.isEmpty()) return Map.of();
        return assetRepository.sumSubAssetPricesGroupedByParent(ids).stream()
                .collect(Collectors.toMap(t -> (Long) t.get(0), t -> (BigDecimal) t.get(1), (a, b) -> b));
    }

    private Map<Long, String> loadFirstPictureUrls(List<Asset> assets) {
        List<Long> ids = assets.stream().map(Asset::getId).toList();
        if (ids.isEmpty()) return Map.of();
        return assetPictureRepository.findFirstPictureIdGroupedByAsset(ids).stream()
                .collect(Collectors.toMap(
                        t -> (Long) t.get("assetId"),
                        t -> OssUrlBuilder.build((String) t.get("storedFilename"), (String) t.get("originalFilename")),
                        (a, b) -> b));
    }

    @Transactional(readOnly = true)
    public AssetDetailResponse getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));

        List<Asset> subAssetEntities = assetRepository.findByParentId(id);
        List<AssetResponse> subAssets = subAssetEntities.stream()
                .map(sub -> AssetResponse.from(sub, computeWarrantyStatus(sub), 0))
                .toList();

        List<AssetInvoiceResponse> invoices = assetInvoiceRepository.findByAssetId(id).stream()
                .map(AssetInvoiceResponse::from)
                .toList();

        List<AssetAttachmentResponse> attachments = assetAttachmentRepository.findByAssetId(id).stream()
                .map(a -> AssetAttachmentResponse.from(a, fileService.isIndexed(a.getFile().getId())))
                .toList();

        AssetDetailResponse detail = AssetDetailResponse.from(asset, computeWarrantyStatus(asset), subAssets.size(), subAssets, invoices);
        detail.setAttachments(attachments);
        return detail;
    }

    @Transactional
    public AssetDetailResponse createAsset(CreateAssetRequest request) {
        if (request.getBarcode() != null && request.getSerialNumber() != null
                && assetRepository.existsByBarcodeAndSerialNumber(request.getBarcode(), request.getSerialNumber())) {
            throw new ResourceAlreadyExistsException("Asset already exists with barcode: " + request.getBarcode() + " and serial number: " + request.getSerialNumber());
        }

        AssetCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        AssetPlace place = placeRepository.findById(request.getPlaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Place not found with id: " + request.getPlaceId()));

        Asset asset = new Asset();
        asset.setName(request.getName());
        asset.setBarcode(request.getBarcode());
        asset.setSerialNumber(request.getSerialNumber());
        asset.setCategory(category);
        asset.setPlace(place);
        boolean inUse = request.getInUse() != null ? request.getInUse() : true;
        asset.setInUse(inUse);
        if (!inUse) {
            if (request.getRetireDate() == null) {
                throw new OperationNotAllowedException("Retire date is required when asset is not in use");
            }
            if (request.getRetireDate().isAfter(LocalDate.now())) {
                throw new OperationNotAllowedException("Retire date must not be in the future");
            }
            asset.setRetireDate(request.getRetireDate());
        }
        asset.setPrice(request.getPrice());
        asset.setShopDate(request.getShopDate());

        if (request.getStoreId() != null) {
            AssetStore store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + request.getStoreId()));
            asset.setStore(store);
        }

        asset.setHasWarranty(request.getHasWarranty() != null ? request.getHasWarranty() : false);
        asset.setNote(request.getNote());

        if (request.getParentId() != null) {
            Asset parent = assetRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent asset not found with id: " + request.getParentId()));
            if (parent.getParent() != null) {
                throw new OperationNotAllowedException("Cannot create sub-asset under another sub-asset");
            }
            asset.setParent(parent);
        }

        if (asset.isHasWarranty()) {
            DateCalculator.DateTriplet dates = DateCalculator.calculate(
                    request.getActiveDate(), request.getExpirationDate(), request.getWarrantyPeriod());
            asset.setActiveDate(dates.startDate());
            asset.setExpirationDate(dates.endDate());
            asset.setWarrantyPeriod(dates.durationDays());
        }

        Asset saved = assetRepository.save(asset);
        return AssetDetailResponse.from(saved, computeWarrantyStatus(saved), 0, List.of());
    }

    @Transactional
    public AssetDetailResponse updateAsset(Long id, UpdateAssetRequest request) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));

        if (request.getName() != null) {
            asset.setName(request.getName());
        }

        if (request.getBarcode() != null || request.getSerialNumber() != null) {
            String newBarcode = request.getBarcode() != null ? request.getBarcode() : asset.getBarcode();
            String newSerial = request.getSerialNumber() != null ? request.getSerialNumber() : asset.getSerialNumber();
            if (newBarcode != null && newSerial != null
                    && (!newBarcode.equals(asset.getBarcode()) || !newSerial.equals(asset.getSerialNumber()))
                    && assetRepository.existsByBarcodeAndSerialNumber(newBarcode, newSerial)) {
                throw new ResourceAlreadyExistsException("Asset already exists with barcode: " + newBarcode + " and serial number: " + newSerial);
            }
            if (request.getBarcode() != null) asset.setBarcode(request.getBarcode());
            if (request.getSerialNumber() != null) asset.setSerialNumber(request.getSerialNumber());
        }

        if (request.getCategoryId() != null) {
            AssetCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            asset.setCategory(category);
        }

        if (request.getPlaceId() != null) {
            AssetPlace place = placeRepository.findById(request.getPlaceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Place not found with id: " + request.getPlaceId()));
            asset.setPlace(place);
        }

        if (request.getInUse() != null) {
            asset.setInUse(request.getInUse());
            if (request.getInUse()) {
                asset.setRetireDate(null);
            } else {
                LocalDate retireDate = request.getRetireDate() != null ? request.getRetireDate() : asset.getRetireDate();
                if (retireDate == null) {
                    throw new OperationNotAllowedException("Retire date is required when asset is not in use");
                }
                if (retireDate.isAfter(LocalDate.now())) {
                    throw new OperationNotAllowedException("Retire date must not be in the future");
                }
                asset.setRetireDate(retireDate);
            }
        } else if (request.getRetireDate() != null) {
            if (request.getRetireDate().isAfter(LocalDate.now())) {
                throw new OperationNotAllowedException("Retire date must not be in the future");
            }
            asset.setRetireDate(request.getRetireDate());
        }

        if (request.getPrice() != null) {
            asset.setPrice(request.getPrice());
        }

        if (request.getShopDate() != null) {
            asset.setShopDate(request.getShopDate());
        }

        if (request.getStoreId() != null) {
            AssetStore store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + request.getStoreId()));
            asset.setStore(store);
        }

        if (request.getHasWarranty() != null) {
            asset.setHasWarranty(request.getHasWarranty());
        }

        if (request.getNote() != null) {
            asset.setNote(request.getNote().isEmpty() ? null : request.getNote());
        }

        if (asset.isHasWarranty()) {
            boolean hasDates = request.getActiveDate() != null || request.getExpirationDate() != null || request.getWarrantyPeriod() != null;
            if (hasDates) {
                LocalDate activeDate = request.getActiveDate() != null ? request.getActiveDate() : asset.getActiveDate();
                LocalDate expirationDate = request.getExpirationDate() != null ? request.getExpirationDate() : asset.getExpirationDate();
                Integer warrantyPeriod = request.getWarrantyPeriod() != null ? request.getWarrantyPeriod() : asset.getWarrantyPeriod();
                DateCalculator.DateTriplet dates = DateCalculator.calculate(activeDate, expirationDate, warrantyPeriod);
                asset.setActiveDate(dates.startDate());
                asset.setExpirationDate(dates.endDate());
                asset.setWarrantyPeriod(dates.durationDays());
            }
        }

        Asset saved = assetRepository.save(asset);
        List<Asset> subAssetEntities = assetRepository.findByParentId(id);
        List<AssetResponse> subAssets = subAssetEntities.stream()
                .map(sub -> AssetResponse.from(sub, computeWarrantyStatus(sub), 0))
                .toList();
        return AssetDetailResponse.from(saved, computeWarrantyStatus(saved), subAssets.size(), subAssets);
    }

    @Transactional
    public void deleteAsset(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));

        if (assetRepository.existsByParentId(asset.getId())) {
            throw new OperationNotAllowedException("Cannot delete asset that has sub-assets. Delete all sub-assets first.");
        }

        for (var attachment : asset.getAttachments()) {
            fileService.delete(attachment.getFile().getId());
        }

        for (var picture : asset.getPictures()) {
            fileService.delete(picture.getFile().getId());
        }

        assetRepository.delete(asset);
    }

    public WarrantyStatus computeWarrantyStatus(Asset asset) {
        if (!asset.isHasWarranty()) return WarrantyStatus.NO_WARRANTY;
        if (asset.getExpirationDate() == null) return WarrantyStatus.NO_WARRANTY;
        if (asset.getExpirationDate().isBefore(LocalDate.now())) return WarrantyStatus.OUT_WARRANTY;
        return WarrantyStatus.IN_WARRANTY;
    }

    @Transactional(readOnly = true)
    public List<AssetInvoiceResponse> getAssetInvoices(Long assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found with id: " + assetId);
        }
        return assetInvoiceRepository.findByAssetId(assetId).stream()
                .map(AssetInvoiceResponse::from)
                .toList();
    }

    @Transactional
    public void bindInvoice(Long assetId, Long invoiceId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + assetId));
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        if (assetInvoiceRepository.existsByAssetIdAndInvoiceId(assetId, invoiceId)) {
            throw new ResourceAlreadyExistsException("Invoice already bound to this asset");
        }

        AssetInvoice binding = new AssetInvoice();
        binding.setAsset(asset);
        binding.setInvoice(invoice);
        assetInvoiceRepository.save(binding);
    }

    @Transactional
    public void unbindInvoice(Long assetId, Long invoiceId) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found with id: " + assetId);
        }
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + invoiceId);
        }
        assetInvoiceRepository.deleteByAssetIdAndInvoiceId(assetId, invoiceId);
    }
}
