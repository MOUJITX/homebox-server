package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateAssetStoreRequest;
import com.moujitx.homebox.server.dto.request.UpdateAssetStoreRequest;
import com.moujitx.homebox.server.dto.response.AssetStoreResponse;
import com.moujitx.homebox.server.entity.AssetStore;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.AssetRepository;
import com.moujitx.homebox.server.repository.AssetStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetStoreService {

    private final AssetStoreRepository assetStoreRepository;
    private final AssetRepository assetRepository;

    public List<AssetStoreResponse> getAllStores() {
        return assetStoreRepository.findAll().stream()
                .map(AssetStoreResponse::from)
                .toList();
    }

    public AssetStoreResponse getStoreById(Long id) {
        AssetStore store = assetStoreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset store not found with id: " + id));
        return AssetStoreResponse.from(store);
    }

    @Transactional
    public AssetStoreResponse createStore(CreateAssetStoreRequest request) {
        if (assetStoreRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Asset store already exists: " + request.getName());
        }

        AssetStore store = new AssetStore(request.getName(), request.getChannel());
        return AssetStoreResponse.from(assetStoreRepository.save(store));
    }

    @Transactional
    public AssetStoreResponse updateStore(Long id, UpdateAssetStoreRequest request) {
        AssetStore store = assetStoreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset store not found with id: " + id));

        if (request.getName() != null) {
            if (!request.getName().equals(store.getName()) && assetStoreRepository.existsByName(request.getName())) {
                throw new ResourceAlreadyExistsException("Asset store already exists: " + request.getName());
            }
            store.setName(request.getName());
        }

        if (request.getChannel() != null) {
            store.setChannel(request.getChannel().isEmpty() ? null : request.getChannel());
        }

        return AssetStoreResponse.from(assetStoreRepository.save(store));
    }

    @Transactional
    public void deleteStore(Long id) {
        AssetStore store = assetStoreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset store not found with id: " + id));

        if (assetRepository.existsByStoreId(store.getId())) {
            throw new OperationNotAllowedException("Cannot delete asset store that is used by assets");
        }

        assetStoreRepository.delete(store);
    }
}
