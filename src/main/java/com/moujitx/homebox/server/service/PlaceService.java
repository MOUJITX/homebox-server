package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreatePlaceRequest;
import com.moujitx.homebox.server.dto.request.UpdatePlaceRequest;
import com.moujitx.homebox.server.dto.response.PlaceResponse;
import com.moujitx.homebox.server.entity.Place;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.AssetRepository;
import com.moujitx.homebox.server.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final AssetRepository assetRepository;

    public List<PlaceResponse> getAllPlaces() {
        return placeRepository.findAll().stream()
                .map(PlaceResponse::from)
                .toList();
    }

    public PlaceResponse getPlaceById(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Place not found with id: " + id));
        return PlaceResponse.from(place);
    }

    @Transactional
    public PlaceResponse createPlace(CreatePlaceRequest request) {
        if (placeRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Place already exists: " + request.getName());
        }

        Place place = new Place(request.getName(), request.getDescription());
        return PlaceResponse.from(placeRepository.save(place));
    }

    @Transactional
    public PlaceResponse updatePlace(Long id, UpdatePlaceRequest request) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Place not found with id: " + id));

        if (request.getName() != null) {
            if (!request.getName().equals(place.getName()) && placeRepository.existsByName(request.getName())) {
                throw new ResourceAlreadyExistsException("Place already exists: " + request.getName());
            }
            place.setName(request.getName());
        }

        if (request.getDescription() != null) {
            place.setDescription(request.getDescription().isEmpty() ? null : request.getDescription());
        }

        return PlaceResponse.from(placeRepository.save(place));
    }

    @Transactional
    public void deletePlace(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Place not found with id: " + id));

        if (assetRepository.existsByPlaceId(place.getId())) {
            throw new OperationNotAllowedException("Cannot delete place that is used by assets");
        }

        placeRepository.delete(place);
    }
}
