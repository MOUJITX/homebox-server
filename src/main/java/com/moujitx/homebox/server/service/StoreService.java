package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateStoreRequest;
import com.moujitx.homebox.server.dto.request.UpdateStoreRequest;
import com.moujitx.homebox.server.dto.response.StoreResponse;
import com.moujitx.homebox.server.entity.Store;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.AssetRepository;
import com.moujitx.homebox.server.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final AssetRepository assetRepository;

    public List<StoreResponse> getAllStores() {
        return storeRepository.findAll().stream()
                .map(StoreResponse::from)
                .toList();
    }

    public StoreResponse getStoreById(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + id));
        return StoreResponse.from(store);
    }

    @Transactional
    public StoreResponse createStore(CreateStoreRequest request) {
        if (storeRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Store already exists: " + request.getName());
        }

        Store store = new Store(request.getName(), request.getChannel());
        return StoreResponse.from(storeRepository.save(store));
    }

    @Transactional
    public StoreResponse updateStore(Long id, UpdateStoreRequest request) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + id));

        if (request.getName() != null) {
            if (!request.getName().equals(store.getName()) && storeRepository.existsByName(request.getName())) {
                throw new ResourceAlreadyExistsException("Store already exists: " + request.getName());
            }
            store.setName(request.getName());
        }

        if (request.getChannel() != null) {
            store.setChannel(request.getChannel().isEmpty() ? null : request.getChannel());
        }

        return StoreResponse.from(storeRepository.save(store));
    }

    @Transactional
    public void deleteStore(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + id));

        if (assetRepository.existsByStoreId(store.getId())) {
            throw new OperationNotAllowedException("Cannot delete store that is used by assets");
        }

        storeRepository.delete(store);
    }
}
