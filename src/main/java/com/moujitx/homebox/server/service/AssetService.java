package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateAssetRequest;
import com.moujitx.homebox.server.dto.request.UpdateAssetRequest;
import com.moujitx.homebox.server.dto.response.AssetDetailResponse;
import com.moujitx.homebox.server.dto.response.AssetResponse;
import com.moujitx.homebox.server.entity.*;
import com.moujitx.homebox.server.enums.WarrantyStatus;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.*;
import com.moujitx.homebox.server.util.DateCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetCategoryRepository categoryRepository;
    private final AssetPlaceRepository placeRepository;
    private final AssetStoreRepository storeRepository;
    private final AssetPictureRepository assetPictureRepository;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public Page<AssetResponse> getAssets(String search, Long categoryId, Long placeId, Boolean isInUse,
                                          WarrantyStatus warrantyStatus, Pageable pageable) {
        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;

        if (warrantyStatus != null) {
            List<Asset> allAssets = assetRepository.findWithFilters(searchParam, categoryId, placeId, isInUse, Pageable.unpaged()).getContent();
            List<AssetResponse> filtered = allAssets.stream()
                    .filter(asset -> computeWarrantyStatus(asset) == warrantyStatus)
                    .map(asset -> AssetResponse.from(asset, computeWarrantyStatus(asset), assetRepository.findByParentId(asset.getId()).size()))
                    .toList();

            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), filtered.size());
            List<AssetResponse> pageContent = start < filtered.size() ? filtered.subList(start, end) : List.of();
            return new PageImpl<>(pageContent, pageable, filtered.size());
        }

        Page<Asset> assetsPage = assetRepository.findWithFilters(searchParam, categoryId, placeId, isInUse, pageable);
        List<AssetResponse> responses = assetsPage.getContent().stream()
                .map(asset -> AssetResponse.from(asset, computeWarrantyStatus(asset), assetRepository.findByParentId(asset.getId()).size()))
                .toList();
        return new PageImpl<>(responses, pageable, assetsPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public AssetDetailResponse getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));

        List<Asset> subAssetEntities = assetRepository.findByParentId(id);
        List<AssetResponse> subAssets = subAssetEntities.stream()
                .map(sub -> AssetResponse.from(sub, computeWarrantyStatus(sub), 0))
                .toList();

        return AssetDetailResponse.from(asset, computeWarrantyStatus(asset), subAssets.size(), subAssets);
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
        asset.setInUse(request.getIsInUse() != null ? request.getIsInUse() : true);
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

        if (request.getIsInUse() != null) {
            asset.setInUse(request.getIsInUse());
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
}
