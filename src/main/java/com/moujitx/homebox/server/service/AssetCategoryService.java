package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateAssetCategoryRequest;
import com.moujitx.homebox.server.dto.request.UpdateAssetCategoryRequest;
import com.moujitx.homebox.server.dto.response.AssetCategoryResponse;
import com.moujitx.homebox.server.entity.AssetCategory;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.AssetCategoryRepository;
import com.moujitx.homebox.server.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetCategoryService {

    private final AssetCategoryRepository assetCategoryRepository;
    private final AssetRepository assetRepository;

    public List<AssetCategoryResponse> getAllCategories() {
        return assetCategoryRepository.findAll().stream()
                .map(AssetCategoryResponse::from)
                .toList();
    }

    public AssetCategoryResponse getCategoryById(Long id) {
        AssetCategory category = assetCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset category not found with id: " + id));
        return AssetCategoryResponse.from(category);
    }

    @Transactional
    public AssetCategoryResponse createCategory(CreateAssetCategoryRequest request) {
        if (assetCategoryRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Asset category already exists: " + request.getName());
        }

        AssetCategory category = new AssetCategory(request.getName(), request.getDescription());
        return AssetCategoryResponse.from(assetCategoryRepository.save(category));
    }

    @Transactional
    public AssetCategoryResponse updateCategory(Long id, UpdateAssetCategoryRequest request) {
        AssetCategory category = assetCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset category not found with id: " + id));

        if (request.getName() != null) {
            if (!request.getName().equals(category.getName()) && assetCategoryRepository.existsByName(request.getName())) {
                throw new ResourceAlreadyExistsException("Asset category already exists: " + request.getName());
            }
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription().isEmpty() ? null : request.getDescription());
        }

        return AssetCategoryResponse.from(assetCategoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        AssetCategory category = assetCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset category not found with id: " + id));

        if (assetRepository.existsByCategoryId(category.getId())) {
            throw new OperationNotAllowedException("Cannot delete asset category that is used by assets");
        }

        assetCategoryRepository.delete(category);
    }
}
