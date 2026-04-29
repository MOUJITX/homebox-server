package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateGoodRequest;
import com.moujitx.homebox.server.dto.request.UpdateGoodRequest;
import com.moujitx.homebox.server.dto.response.GoodDetailResponse;
import com.moujitx.homebox.server.dto.response.GoodResponse;
import com.moujitx.homebox.server.entity.Good;
import com.moujitx.homebox.server.entity.GoodBrand;
import com.moujitx.homebox.server.entity.GoodCategory;
import com.moujitx.homebox.server.entity.GoodItem;
import com.moujitx.homebox.server.enums.GoodStatus;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.GoodBrandRepository;
import com.moujitx.homebox.server.repository.GoodCategoryRepository;
import com.moujitx.homebox.server.repository.GoodItemRepository;
import com.moujitx.homebox.server.repository.GoodRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
public class GoodService {

    private final GoodRepository goodRepository;
    private final GoodCategoryRepository categoryRepository;
    private final GoodBrandRepository brandRepository;
    private final GoodItemRepository itemRepository;

    public GoodService(GoodRepository goodRepository,
                       GoodCategoryRepository categoryRepository,
                       GoodBrandRepository brandRepository,
                       GoodItemRepository itemRepository) {
        this.goodRepository = goodRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public Page<GoodResponse> getGoods(String search, Long categoryId, Long brandId, GoodStatus status, Pageable pageable) {
        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;

        if (status != null) {
            List<Good> allGoods = goodRepository.findWithFilters(searchParam, categoryId, brandId, Pageable.unpaged()).getContent();
            List<GoodResponse> filtered = allGoods.stream()
                    .map(good -> {
                        GoodStatus computed = computeStatus(good);
                        if (computed != status) return null;
                        return GoodResponse.from(good, computed);
                    })
                    .filter(Objects::nonNull)
                    .toList();

            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), filtered.size());
            List<GoodResponse> pageContent = start < filtered.size() ? filtered.subList(start, end) : List.of();
            return new PageImpl<>(pageContent, pageable, filtered.size());
        }

        Page<Good> goodsPage = goodRepository.findWithFilters(searchParam, categoryId, brandId, pageable);
        List<GoodResponse> responses = goodsPage.getContent().stream()
                .map(good -> GoodResponse.from(good, computeStatus(good)))
                .toList();
        return new PageImpl<>(responses, pageable, goodsPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public GoodDetailResponse getGoodById(Long id) {
        Good good = goodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Good not found with id: " + id));
        return GoodDetailResponse.from(good, computeStatus(good));
    }

    @Transactional(readOnly = true)
    public GoodResponse getGoodByBarcode(String barcode) {
        Good good = goodRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Good not found with barcode: " + barcode));
        return GoodResponse.from(good, computeStatus(good));
    }

    @Transactional
    public GoodDetailResponse createGood(CreateGoodRequest request) {
        if (goodRepository.existsByBarcode(request.getBarcode())) {
            throw new ResourceAlreadyExistsException("Good already exists with barcode: " + request.getBarcode());
        }

        GoodCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        GoodBrand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));

        Good good = new Good();
        good.setProductName(request.getProductName());
        good.setBarcode(request.getBarcode());
        good.setCategory(category);
        good.setBrand(brand);
        if (request.getExpiringSoonDays() != null) {
            good.setExpiringSoonDays(request.getExpiringSoonDays());
        }

        Good saved = goodRepository.save(good);
        return GoodDetailResponse.from(saved, computeStatus(saved));
    }

    @Transactional
    public GoodDetailResponse updateGood(Long id, UpdateGoodRequest request) {
        Good good = goodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Good not found with id: " + id));

        if (request.getProductName() != null) {
            good.setProductName(request.getProductName());
        }

        if (request.getBarcode() != null) {
            if (!request.getBarcode().equals(good.getBarcode()) && goodRepository.existsByBarcode(request.getBarcode())) {
                throw new ResourceAlreadyExistsException("Good already exists with barcode: " + request.getBarcode());
            }
            good.setBarcode(request.getBarcode());
        }

        if (request.getCategoryId() != null) {
            GoodCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            good.setCategory(category);
        }

        if (request.getBrandId() != null) {
            GoodBrand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));
            good.setBrand(brand);
        }

        if (request.getExpiringSoonDays() != null) {
            good.setExpiringSoonDays(request.getExpiringSoonDays());
        }

        Good saved = goodRepository.save(good);
        return GoodDetailResponse.from(saved, computeStatus(saved));
    }

    @Transactional
    public void deleteGood(Long id) {
        Good good = goodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Good not found with id: " + id));

        if (itemRepository.existsByGoodId(good.getId())) {
            throw new OperationNotAllowedException("Cannot delete good that has items. Delete all items first.");
        }

        goodRepository.delete(good);
    }

    public GoodStatus computeStatus(Good good) {
        List<GoodItem> items = good.getItems();
        if (items.isEmpty()) return GoodStatus.EXHAUSTED;

        LocalDate today = LocalDate.now();
        boolean hasExpired = false;
        boolean hasExpiringSoon = false;
        boolean hasInUse = false;

        for (GoodItem item : items) {
            if (item.getExpirationDate().isBefore(today)) {
                hasExpired = true;
                break;
            }
            if (item.isInUse()) {
                hasInUse = true;
                long daysUntilExpiration = ChronoUnit.DAYS.between(today, item.getExpirationDate());
                if (daysUntilExpiration <= good.getExpiringSoonDays()) {
                    hasExpiringSoon = true;
                }
            }
        }

        if (hasExpired) return GoodStatus.EXPIRED;
        if (hasExpiringSoon) return GoodStatus.EXPIRING_SOON;
        if (!hasInUse) return GoodStatus.EXHAUSTED;
        return GoodStatus.IN_USE;
    }
}
