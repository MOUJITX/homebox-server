package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateBookLocationRequest;
import com.moujitx.homebox.server.dto.request.UpdateBookLocationRequest;
import com.moujitx.homebox.server.dto.response.BookLocationResponse;
import com.moujitx.homebox.server.entity.BookLocation;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.BookLocationRepository;
import com.moujitx.homebox.server.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookLocationService {

    private final BookLocationRepository bookLocationRepository;
    private final BookRepository bookRepository;

    public List<BookLocationResponse> getAllLocations() {
        return bookLocationRepository.findAll().stream()
                .map(BookLocationResponse::from)
                .toList();
    }

    @Transactional
    public BookLocationResponse createLocation(CreateBookLocationRequest request) {
        if (bookLocationRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Book location already exists: " + request.getName());
        }

        BookLocation location = new BookLocation(request.getName(), request.getDescription());
        return BookLocationResponse.from(bookLocationRepository.save(location));
    }

    @Transactional
    public BookLocationResponse updateLocation(Long id, UpdateBookLocationRequest request) {
        BookLocation location = bookLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book location not found with id: " + id));

        if (request.getName() != null) {
            if (!request.getName().equals(location.getName()) && bookLocationRepository.existsByName(request.getName())) {
                throw new ResourceAlreadyExistsException("Book location already exists: " + request.getName());
            }
            location.setName(request.getName());
        }

        if (request.getDescription() != null) {
            location.setDescription(request.getDescription().isEmpty() ? null : request.getDescription());
        }

        return BookLocationResponse.from(bookLocationRepository.save(location));
    }

    @Transactional
    public void deleteLocation(Long id) {
        BookLocation location = bookLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book location not found with id: " + id));

        if (bookRepository.existsByLocationId(location.getId())) {
            throw new OperationNotAllowedException("Cannot delete book location that is used by books");
        }

        bookLocationRepository.delete(location);
    }
}
