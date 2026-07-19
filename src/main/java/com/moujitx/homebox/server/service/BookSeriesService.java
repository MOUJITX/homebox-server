package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateBookSeriesRequest;
import com.moujitx.homebox.server.dto.request.UpdateBookSeriesRequest;
import com.moujitx.homebox.server.dto.response.BookSeriesResponse;
import com.moujitx.homebox.server.entity.BookSeries;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.BookSeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookSeriesService {

    private final BookSeriesRepository bookSeriesRepository;

    public List<BookSeriesResponse> getAllSeries() {
        return bookSeriesRepository.findAll().stream()
                .map(BookSeriesResponse::from)
                .toList();
    }

    @Transactional
    public BookSeriesResponse createSeries(CreateBookSeriesRequest request) {
        if (bookSeriesRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Book series already exists: " + request.getName());
        }

        BookSeries series = new BookSeries(request.getName(), request.getDescription());
        return BookSeriesResponse.from(bookSeriesRepository.save(series));
    }

    @Transactional
    public BookSeriesResponse updateSeries(Long id, UpdateBookSeriesRequest request) {
        BookSeries series = bookSeriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book series not found with id: " + id));

        if (request.getName() != null) {
            if (!request.getName().equals(series.getName()) && bookSeriesRepository.existsByName(request.getName())) {
                throw new ResourceAlreadyExistsException("Book series already exists: " + request.getName());
            }
            series.setName(request.getName());
        }

        if (request.getDescription() != null) {
            series.setDescription(request.getDescription().isEmpty() ? null : request.getDescription());
        }

        return BookSeriesResponse.from(bookSeriesRepository.save(series));
    }

    @Transactional
    public void deleteSeries(Long id) {
        if (!bookSeriesRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book series not found with id: " + id);
        }
        bookSeriesRepository.deleteById(id);
    }
}
