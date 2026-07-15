package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.CreateBookRequest;
import com.moujitx.homebox.server.dto.request.UpdateBookRequest;
import com.moujitx.homebox.server.dto.response.*;
import com.moujitx.homebox.server.service.BookService;
import com.moujitx.homebox.server.service.DoubanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final DoubanService doubanService;

    @GetMapping
    public ResponseEntity<Page<BookResponse>> getBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(bookService.getBooks(search, categoryId, locationId, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDetailResponse> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @PostMapping
    public ResponseEntity<BookDetailResponse> createBook(@Valid @RequestBody CreateBookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.createBook(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDetailResponse> updateBook(@PathVariable Long id,
                                                          @Valid @RequestBody UpdateBookRequest request) {
        return ResponseEntity.ok(bookService.updateBook(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<List<BookResponse>> getChildren(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getChildren(id));
    }

    @GetMapping("/{id}/pictures")
    public ResponseEntity<List<BookPictureResponse>> getBookPictures(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookPictures(id));
    }

    @PostMapping("/{id}/pictures")
    public ResponseEntity<BookPictureResponse> uploadPicture(@PathVariable Long id,
                                                              @RequestParam Long fileId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.uploadPicture(id, fileId));
    }

    @DeleteMapping("/{bookId}/pictures/{pictureId}")
    public ResponseEntity<Void> deletePicture(@PathVariable Long bookId, @PathVariable Long pictureId) {
        bookService.deletePicture(bookId, pictureId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/invoices")
    public ResponseEntity<List<BookInvoiceResponse>> getBookInvoices(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookInvoices(id));
    }

    @PostMapping("/{bookId}/invoices/{invoiceId}")
    public ResponseEntity<Void> bindInvoice(@PathVariable Long bookId, @PathVariable Long invoiceId) {
        bookService.bindInvoice(bookId, invoiceId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{bookId}/invoices/{invoiceId}")
    public ResponseEntity<Void> unbindInvoice(@PathVariable Long bookId, @PathVariable Long invoiceId) {
        bookService.unbindInvoice(bookId, invoiceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/series")
    public ResponseEntity<List<BookSeriesResponse>> getBookSeries(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookSeriesList(id));
    }

    @PutMapping("/{id}/series")
    public ResponseEntity<List<BookSeriesResponse>> setBookSeries(@PathVariable Long id,
                                                                    @RequestBody List<Long> seriesIds) {
        return ResponseEntity.ok(bookService.setBookSeries(id, seriesIds));
    }

    @GetMapping("/lookup-douban")
    public ResponseEntity<DoubanBookLookupResponse> lookupDouban(
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String issn,
            @RequestParam(required = false) String q) {
        DoubanBookLookupResponse result = null;
        if (isbn != null && !isbn.isEmpty()) {
            result = doubanService.lookupByIsbn(isbn);
        } else if (issn != null && !issn.isEmpty()) {
            result = doubanService.lookupByIsbn(issn);
        } else if (q != null && !q.isEmpty()) {
            result = doubanService.searchBooks(q);
        }
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}
