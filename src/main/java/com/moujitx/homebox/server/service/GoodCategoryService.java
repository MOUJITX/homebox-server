package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateGoodCategoryRequest;
import com.moujitx.homebox.server.dto.request.UpdateGoodCategoryRequest;
import com.moujitx.homebox.server.dto.response.GoodCategoryResponse;
import com.moujitx.homebox.server.entity.GoodCategory;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.GoodCategoryRepository;
import com.moujitx.homebox.server.repository.GoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GoodCategoryService {

    private final GoodCategoryRepository categoryRepository;
    private final GoodRepository goodRepository;

    public GoodCategoryService(GoodCategoryRepository categoryRepository,
                               GoodRepository goodRepository) {
        this.categoryRepository = categoryRepository;
        this.goodRepository = goodRepository;
    }

    public List<GoodCategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(GoodCategoryResponse::from)
                .toList();
    }

    public GoodCategoryResponse getCategoryById(Long id) {
        GoodCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return GoodCategoryResponse.from(category);
    }

    @Transactional
    public GoodCategoryResponse createCategory(CreateGoodCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Category already exists: " + request.getName());
        }

        GoodCategory category = new GoodCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return GoodCategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public GoodCategoryResponse updateCategory(Long id, UpdateGoodCategoryRequest request) {
        GoodCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (request.getName() != null) {
            if (!request.getName().equals(category.getName()) && categoryRepository.existsByName(request.getName())) {
                throw new ResourceAlreadyExistsException("Category already exists: " + request.getName());
            }
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        return GoodCategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        GoodCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (goodRepository.existsByCategoryId(category.getId())) {
            throw new OperationNotAllowedException("Cannot delete category that is used by goods");
        }

        categoryRepository.delete(category);
    }
}
