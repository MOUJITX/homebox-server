package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateGoodItemRequest;
import com.moujitx.homebox.server.dto.request.UpdateGoodItemRequest;
import com.moujitx.homebox.server.dto.response.GoodItemResponse;
import com.moujitx.homebox.server.entity.Good;
import com.moujitx.homebox.server.entity.GoodItem;
import com.moujitx.homebox.server.enums.ItemStatus;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.GoodItemRepository;
import com.moujitx.homebox.server.repository.GoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moujitx.homebox.server.util.DateCalculator;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodItemService {

    private final GoodItemRepository itemRepository;
    private final GoodRepository goodRepository;
    private final GoodService goodService;

    public List<GoodItemResponse> getItemsByGoodId(Long goodId, ItemStatus itemStatus) {
        Good good = goodRepository.findById(goodId)
                .orElseThrow(() -> new ResourceNotFoundException("Good not found with id: " + goodId));
        return itemRepository.findByGoodId(goodId).stream()
                .filter(item -> {
                    if (itemStatus == null) return true;
                    return goodService.computeItemStatus(item, good.getExpiringSoonDays()) == itemStatus;
                })
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
        DateCalculator.DateTriplet result = DateCalculator.calculate(productDate, expirationDate, lifeDays);
        item.setProductDate(result.startDate());
        item.setExpirationDate(result.endDate());
        item.setLifeDays(result.durationDays());
    }
}
