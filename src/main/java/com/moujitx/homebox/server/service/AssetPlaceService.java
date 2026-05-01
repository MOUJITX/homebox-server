package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateAssetPlaceRequest;
import com.moujitx.homebox.server.dto.request.UpdateAssetPlaceRequest;
import com.moujitx.homebox.server.dto.response.AssetPlaceResponse;
import com.moujitx.homebox.server.entity.AssetPlace;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.AssetPlaceRepository;
import com.moujitx.homebox.server.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetPlaceService {

    private final AssetPlaceRepository assetPlaceRepository;
    private final AssetRepository assetRepository;

    public List<AssetPlaceResponse> getAllPlaces() {
        return assetPlaceRepository.findAll().stream()
                .map(AssetPlaceResponse::from)
                .toList();
    }

    public AssetPlaceResponse getPlaceById(Long id) {
        AssetPlace place = assetPlaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset place not found with id: " + id));
        return AssetPlaceResponse.from(place);
    }

    @Transactional
    public AssetPlaceResponse createPlace(CreateAssetPlaceRequest request) {
        if (assetPlaceRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Asset place already exists: " + request.getName());
        }

        AssetPlace place = new AssetPlace(request.getName(), request.getDescription());
        return AssetPlaceResponse.from(assetPlaceRepository.save(place));
    }

    @Transactional
    public AssetPlaceResponse updatePlace(Long id, UpdateAssetPlaceRequest request) {
        AssetPlace place = assetPlaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset place not found with id: " + id));

        if (request.getName() != null) {
            if (!request.getName().equals(place.getName()) && assetPlaceRepository.existsByName(request.getName())) {
                throw new ResourceAlreadyExistsException("Asset place already exists: " + request.getName());
            }
            place.setName(request.getName());
        }

        if (request.getDescription() != null) {
            place.setDescription(request.getDescription().isEmpty() ? null : request.getDescription());
        }

        return AssetPlaceResponse.from(assetPlaceRepository.save(place));
    }

    @Transactional
    public void deletePlace(Long id) {
        AssetPlace place = assetPlaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset place not found with id: " + id));

        if (assetRepository.existsByPlaceId(place.getId())) {
            throw new OperationNotAllowedException("Cannot delete asset place that is used by assets");
        }

        assetPlaceRepository.delete(place);
    }
}
