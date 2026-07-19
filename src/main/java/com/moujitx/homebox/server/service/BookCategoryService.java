package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateBookCategoryRequest;
import com.moujitx.homebox.server.dto.request.UpdateBookCategoryRequest;
import com.moujitx.homebox.server.dto.response.BookCategoryResponse;
import com.moujitx.homebox.server.entity.BookCategory;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.BookCategoryRepository;
import com.moujitx.homebox.server.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookCategoryService {

    private final BookCategoryRepository bookCategoryRepository;
    private final BookRepository bookRepository;

    public List<BookCategoryResponse> getAllCategories() {
        return bookCategoryRepository.findAll().stream()
                .map(BookCategoryResponse::from)
                .toList();
    }

    @Transactional
    public BookCategoryResponse createCategory(CreateBookCategoryRequest request) {
        if (bookCategoryRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Book category already exists: " + request.getName());
        }
        if (bookCategoryRepository.existsByKey(request.getKey())) {
            throw new ResourceAlreadyExistsException("Book category key already exists: " + request.getKey());
        }

        BookCategory category = new BookCategory(
                request.getName(), request.getKey(), request.isSerialized(), request.getDescription());
        return BookCategoryResponse.from(bookCategoryRepository.save(category));
    }

    @Transactional
    public BookCategoryResponse updateCategory(Long id, UpdateBookCategoryRequest request) {
        BookCategory category = bookCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book category not found with id: " + id));

        if (request.getName() != null) {
            if (!request.getName().equals(category.getName()) && bookCategoryRepository.existsByName(request.getName())) {
                throw new ResourceAlreadyExistsException("Book category already exists: " + request.getName());
            }
            category.setName(request.getName());
        }

        if (request.getKey() != null) {
            if (!request.getKey().equals(category.getKey()) && bookCategoryRepository.existsByKey(request.getKey())) {
                throw new ResourceAlreadyExistsException("Book category key already exists: " + request.getKey());
            }
            category.setKey(request.getKey());
        }

        if (request.getSerialized() != null) {
            category.setSerialized(request.getSerialized());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription().isEmpty() ? null : request.getDescription());
        }

        return BookCategoryResponse.from(bookCategoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        BookCategory category = bookCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book category not found with id: " + id));

        if (bookRepository.existsByCategoryId(category.getId())) {
            throw new OperationNotAllowedException("Cannot delete book category that is used by books");
        }

        bookCategoryRepository.delete(category);
    }
}
