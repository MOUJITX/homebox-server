package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateDocumentCategoryRequest;
import com.moujitx.homebox.server.dto.request.UpdateDocumentCategoryRequest;
import com.moujitx.homebox.server.dto.response.DocumentCategoryResponse;
import com.moujitx.homebox.server.entity.DocumentCategory;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.DocumentCategoryRepository;
import com.moujitx.homebox.server.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentCategoryService {

    private final DocumentCategoryRepository categoryRepository;
    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public List<DocumentCategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(DocumentCategoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentCategoryResponse getById(Long id) {
        DocumentCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document category not found with id: " + id));
        return DocumentCategoryResponse.from(category);
    }

    @Transactional
    public DocumentCategoryResponse create(CreateDocumentCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Document category already exists with name: " + request.getName());
        }

        DocumentCategory category = new DocumentCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return DocumentCategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public DocumentCategoryResponse update(Long id, UpdateDocumentCategoryRequest request) {
        DocumentCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document category not found with id: " + id));

        if (request.getName() != null) {
            if (!request.getName().equals(category.getName()) && categoryRepository.existsByName(request.getName())) {
                throw new ResourceAlreadyExistsException("Document category already exists with name: " + request.getName());
            }
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        return DocumentCategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Document category not found with id: " + id);
        }
        if (documentRepository.existsByCategoryId(id)) {
            throw new OperationNotAllowedException("Cannot delete category that has documents. Remove all documents first.");
        }
        categoryRepository.deleteById(id);
    }
}
