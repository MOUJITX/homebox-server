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
import com.moujitx.homebox.server.enums.ItemStatus;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.GoodBrandRepository;
import com.moujitx.homebox.server.repository.GoodCategoryRepository;
import com.moujitx.homebox.server.repository.GoodItemRepository;
import com.moujitx.homebox.server.repository.GoodPictureRepository;
import com.moujitx.homebox.server.repository.GoodRepository;
import com.moujitx.homebox.server.util.OssUrlBuilder;
import com.moujitx.homebox.server.util.StringUtil;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoodService {

    private final GoodRepository goodRepository;
    private final GoodCategoryRepository categoryRepository;
    private final GoodBrandRepository brandRepository;
    private final GoodItemRepository itemRepository;
    private final GoodPictureRepository goodPictureRepository;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public Page<GoodResponse> getGoods(String search, Long categoryId, Long brandId, GoodStatus status, ItemStatus itemStatus, Pageable pageable) {
        String searchParam = StringUtil.normalizeSearch(search);

        if (status != null || itemStatus != null) {
            List<Good> allGoods = goodRepository.findWithFilters(searchParam, categoryId, brandId, Pageable.unpaged()).getContent();
            Map<Long, Integer> itemCountTotals = loadItemCountTotals(allGoods);
            Map<Long, Integer> itemCountInUses = loadItemCountInUses(allGoods);
            Map<Long, String> firstPictureUrls = loadFirstPictureUrls(allGoods);
            List<GoodResponse> filtered = allGoods.stream()
                    .filter(good -> {
                        if (status != null && computeStatus(good) != status) return false;
                        if (itemStatus != null) {
                            return good.getItems().stream()
                                    .anyMatch(item -> computeItemStatus(item, good.getExpiringSoonDays()) == itemStatus);
                        }
                        return true;
                    })
                    .map(good -> GoodResponse.from(good, computeStatus(good), itemCountTotals, itemCountInUses, firstPictureUrls))
                    .toList();

            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), filtered.size());
            List<GoodResponse> pageContent = start < filtered.size() ? filtered.subList(start, end) : List.of();
            return new PageImpl<>(pageContent, pageable, filtered.size());
        }

        Page<Good> goodsPage = goodRepository.findWithFilters(searchParam, categoryId, brandId, pageable);
        List<Good> goods = goodsPage.getContent();
        Map<Long, Integer> itemCountTotals = loadItemCountTotals(goods);
        Map<Long, Integer> itemCountInUses = loadItemCountInUses(goods);
        Map<Long, String> firstPictureUrls = loadFirstPictureUrls(goods);
        List<GoodResponse> responses = goods.stream()
                .map(good -> GoodResponse.from(good, computeStatus(good), itemCountTotals, itemCountInUses, firstPictureUrls))
                .toList();
        return new PageImpl<>(responses, pageable, goodsPage.getTotalElements());
    }

    private Map<Long, Integer> loadItemCountTotals(List<Good> goods) {
        List<Long> ids = goods.stream().map(Good::getId).toList();
        if (ids.isEmpty()) return Map.of();
        return goodRepository.countTotalItemsGroupedByGood(ids).stream()
                .collect(Collectors.toMap(t -> (Long) t.get(0), t -> ((Number) t.get(1)).intValue(), (a, b) -> b));
    }

    private Map<Long, Integer> loadItemCountInUses(List<Good> goods) {
        List<Long> ids = goods.stream().map(Good::getId).toList();
        if (ids.isEmpty()) return Map.of();
        return goodRepository.countInUseItemsGroupedByGood(ids).stream()
                .collect(Collectors.toMap(t -> (Long) t.get(0), t -> ((Number) t.get(1)).intValue(), (a, b) -> b));
    }

    private Map<Long, String> loadFirstPictureUrls(List<Good> goods) {
        List<Long> ids = goods.stream().map(Good::getId).toList();
        if (ids.isEmpty()) return Map.of();
        return goodPictureRepository.findFirstPictureIdGroupedByGood(ids).stream()
                .collect(Collectors.toMap(
                        t -> (Long) t.get("goodId"),
                        t -> OssUrlBuilder.build((String) t.get("storedFilename"), (String) t.get("originalFilename")),
                        (a, b) -> b));
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

        for (var picture : good.getPictures()) {
            fileService.delete(picture.getFile().getId());
        }
    }

    public GoodStatus computeStatus(Good good) {
        List<GoodItem> items = good.getItems();
        LocalDate today = LocalDate.now();

        for (GoodItem item : items) {
            if (item.isInUse() && !item.getExpirationDate().isBefore(today)) {
                return GoodStatus.IN_USE;
            }
        }

        return GoodStatus.NOT_IN_USE;
    }

    public ItemStatus computeItemStatus(GoodItem item, int expiringSoonDays) {
        LocalDate today = LocalDate.now();
        if (!item.isInUse()) return ItemStatus.EXHAUSTED;
        if (item.getExpirationDate().isBefore(today)) return ItemStatus.EXPIRED;
        long daysUntil = ChronoUnit.DAYS.between(today, item.getExpirationDate());
        if (daysUntil <= expiringSoonDays) return ItemStatus.EXPIRING_SOON;
        return ItemStatus.IN_USE;
    }
}
