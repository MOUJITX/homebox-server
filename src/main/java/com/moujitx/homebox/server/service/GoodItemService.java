package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateGoodItemRequest;
import com.moujitx.homebox.server.dto.request.UpdateGoodItemRequest;
import com.moujitx.homebox.server.dto.response.GoodItemResponse;
import com.moujitx.homebox.server.entity.Good;
import com.moujitx.homebox.server.entity.GoodItem;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.GoodItemRepository;
import com.moujitx.homebox.server.repository.GoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class GoodItemService {

    private final GoodItemRepository itemRepository;
    private final GoodRepository goodRepository;

    public GoodItemService(GoodItemRepository itemRepository,
                           GoodRepository goodRepository) {
        this.itemRepository = itemRepository;
        this.goodRepository = goodRepository;
    }

    public List<GoodItemResponse> getItemsByGoodId(Long goodId) {
        if (!goodRepository.existsById(goodId)) {
            throw new ResourceNotFoundException("Good not found with id: " + goodId);
        }
        return itemRepository.findByGoodId(goodId).stream()
                .map(GoodItemResponse::from)
                .toList();
    }

    @Transactional
    public GoodItemResponse createItem(Long goodId, CreateGoodItemRequest request) {
        Good good = goodRepository.findById(goodId)
                .orElseThrow(() -> new ResourceNotFoundException("Good not found with id: " + goodId));

        GoodItem item = new GoodItem();
        item.setGood(good);
        calculateAndSetDates(item, request.getProductDate(), request.getExpirationDate(), request.getLifeDays());
        item.setInUse(request.getInUse() != null ? request.getInUse() : true);

        return GoodItemResponse.from(itemRepository.save(item));
    }

    @Transactional
    public GoodItemResponse updateItem(Long goodId, Long itemId, UpdateGoodItemRequest request) {
        if (!goodRepository.existsById(goodId)) {
            throw new ResourceNotFoundException("Good not found with id: " + goodId);
        }

        GoodItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        if (!item.getGood().getId().equals(goodId)) {
            throw new ResourceNotFoundException("Item not found with id: " + itemId + " for good: " + goodId);
        }

        if (request.getInUse() != null) {
            item.setInUse(request.getInUse());
        }

        boolean hasDates = request.getProductDate() != null || request.getExpirationDate() != null || request.getLifeDays() != null;
        if (hasDates) {
            LocalDate productDate = request.getProductDate() != null ? request.getProductDate() : item.getProductDate();
            LocalDate expirationDate = request.getExpirationDate() != null ? request.getExpirationDate() : item.getExpirationDate();
            Integer lifeDays = request.getLifeDays() != null ? request.getLifeDays() : item.getLifeDays();
            calculateAndSetDates(item, productDate, expirationDate, lifeDays);
        }

        return GoodItemResponse.from(itemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long goodId, Long itemId) {
        if (!goodRepository.existsById(goodId)) {
            throw new ResourceNotFoundException("Good not found with id: " + goodId);
        }

        GoodItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        if (!item.getGood().getId().equals(goodId)) {
            throw new ResourceNotFoundException("Item not found with id: " + itemId + " for good: " + goodId);
        }

        itemRepository.delete(item);
    }

    private void calculateAndSetDates(GoodItem item, LocalDate productDate, LocalDate expirationDate, Integer lifeDays) {
        int providedCount = 0;
        if (productDate != null) providedCount++;
        if (expirationDate != null) providedCount++;
        if (lifeDays != null) providedCount++;

        if (providedCount < 2) {
            throw new IllegalArgumentException("At least 2 of (productDate, expirationDate, lifeDays) must be provided");
        }

        if (productDate != null && expirationDate != null) {
            item.setProductDate(productDate);
            item.setExpirationDate(expirationDate);
            item.setLifeDays((int) ChronoUnit.DAYS.between(productDate, expirationDate));
        } else if (productDate != null && lifeDays != null) {
            item.setProductDate(productDate);
            item.setLifeDays(lifeDays);
            item.setExpirationDate(productDate.plusDays(lifeDays));
        } else {
            item.setExpirationDate(expirationDate);
            item.setLifeDays(lifeDays);
            item.setProductDate(expirationDate.minusDays(lifeDays));
        }
    }
}
