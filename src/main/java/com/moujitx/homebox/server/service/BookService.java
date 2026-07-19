package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateBookRequest;
import com.moujitx.homebox.server.dto.request.UpdateBookRequest;
import com.moujitx.homebox.server.dto.response.*;
import com.moujitx.homebox.server.entity.*;
import com.moujitx.homebox.server.enums.BookStatus;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookCategoryRepository bookCategoryRepository;
    private final BookLocationRepository bookLocationRepository;
    private final BookSeriesRepository bookSeriesRepository;
    private final BookSeriesMappingRepository bookSeriesMappingRepository;
    private final BookPictureRepository bookPictureRepository;
    private final BookInvoiceRepository bookInvoiceRepository;
    private final InvoiceRepository invoiceRepository;
    private final FileRecordRepository fileRecordRepository;

    @Transactional(readOnly = true)
    public Page<BookResponse> getBooks(String search, Long categoryId, Long locationId,
                                        String status, Pageable pageable) {
        BookStatus bookStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                bookStatus = BookStatus.valueOf(status);
            } catch (IllegalArgumentException ignored) {
            }
        }

        Page<Book> books = bookRepository.findTopLevelBooks(search, categoryId, locationId, bookStatus, pageable);
        List<Long> bookIds = books.getContent().stream().map(Book::getId).toList();
        List<Long> bookIdsWithChildren = bookIds.stream()
                .filter(id -> bookRepository.countByParentId(id) > 0)
                .toList();

        return books.map(book -> {
            BookResponse r = BookResponse.from(book);
            if (bookIdsWithChildren.contains(book.getId())) {
                r.childCount = (int) bookRepository.countByParentId(book.getId());
            }
            return r;
        });
    }

    @Transactional(readOnly = true)
    public BookDetailResponse getBookById(Long id) {
        Book book = bookRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        List<BookResponse> children = bookRepository.findByParentIdOrderByIssueNumberDesc(id).stream()
                .map(BookResponse::from)
                .toList();

        List<BookSeriesResponse> series = bookSeriesMappingRepository.findByBookId(id).stream()
                .map(m -> BookSeriesResponse.from(m.getSeries()))
                .toList();

        List<BookInvoiceResponse> invoices = bookInvoiceRepository.findByBookId(id).stream()
                .map(BookInvoiceResponse::from)
                .toList();

        return BookDetailResponse.from(book, children, series, invoices);
    }

    @Transactional(readOnly = true)
    public IsbnCheckResponse checkIsbn(String isbn) {
        long count = bookRepository.countByIsbn(isbn);
        List<String> titles = bookRepository.findByIsbn(isbn).stream()
                .map(Book::getTitle)
                .toList();
        return new IsbnCheckResponse(count, titles);
    }

    @Transactional
    public BookDetailResponse createBook(CreateBookRequest request) {
        BookCategory category = bookCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Book category not found with id: " + request.getCategoryId()));

        BookLocation location = bookLocationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Book location not found with id: " + request.getLocationId()));

        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(emptyToNull(request.getAuthor()));
        book.setIsbn(emptyToNull(request.getIsbn()));
        book.setSerialized(request.isSerialized());
        book.setPublisher(emptyToNull(request.getPublisher()));
        book.setPublishDate(request.getPublishDate());
        book.setDescription(emptyToNull(request.getDescription()));
        book.setCategory(category);
        book.setLocation(location);

        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            try {
                book.setStatus(BookStatus.valueOf(request.getStatus()));
            } catch (IllegalArgumentException ignored) {
                book.setStatus(BookStatus.WANT_TO_READ);
            }
        }

        book.setPurchaseDate(request.getPurchaseDate());
        book.setPurchasePrice(request.getPurchasePrice());
        book.setNote(emptyToNull(request.getNote()));

        if (request.getParentId() != null) {
            Book parent = bookRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent book not found with id: " + request.getParentId()));
            if (!parent.isSerialized()) {
                throw new OperationNotAllowedException("Parent book is not serialized");
            }
            if (request.getIssueNumber() == null || request.getIssueNumber().isBlank()) {
                throw new OperationNotAllowedException("Issue number is required for child books");
            }
            book.setParent(parent);
            book.setIssueNumber(request.getIssueNumber().trim());
            book.setSerialized(false);
            book.setCustomBarcode(parent.getCustomBarcode() + "-" + request.getIssueNumber().trim());
        } else {
            book.setIssueNumber(null);
            book.setCustomBarcode(generateBarcode(category));
        }

        if (bookRepository.existsByCustomBarcode(book.getCustomBarcode())) {
            throw new ResourceAlreadyExistsException("Custom barcode already exists: " + book.getCustomBarcode());
        }

        book = bookRepository.save(book);

        if (request.getSeriesIds() != null && !request.getSeriesIds().isEmpty()) {
            attachSeries(book, request.getSeriesIds());
        }

        return getBookById(book.getId());
    }

    @Transactional
    public BookDetailResponse updateBook(Long id, UpdateBookRequest request) {
        Book book = bookRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        if (request.getTitle() != null) book.setTitle(request.getTitle());
        if (request.getAuthor() != null) book.setAuthor(emptyToNull(request.getAuthor()));
        if (request.getIsbn() != null) book.setIsbn(emptyToNull(request.getIsbn()));
        if (request.getSerialized() != null) book.setSerialized(request.getSerialized());
        if (request.getPublisher() != null) book.setPublisher(emptyToNull(request.getPublisher()));
        if (request.getPublishDate() != null) book.setPublishDate(request.getPublishDate());
        if (request.getDescription() != null) book.setDescription(emptyToNull(request.getDescription()));
        if (request.getStatus() != null) {
            try {
                book.setStatus(BookStatus.valueOf(request.getStatus()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (request.getPurchaseDate() != null) book.setPurchaseDate(request.getPurchaseDate());
        if (request.getPurchasePrice() != null) book.setPurchasePrice(request.getPurchasePrice());
        if (request.getNote() != null) book.setNote(emptyToNull(request.getNote()));

        if (request.getCategoryId() != null) {
            BookCategory category = bookCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book category not found"));
            book.setCategory(category);
        }

        if (request.getLocationId() != null) {
            BookLocation location = bookLocationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book location not found"));
            book.setLocation(location);
        }

        if (request.getIssueNumber() != null && book.getParent() != null) {
            String newBarcode = book.getParent().getCustomBarcode() + "-" + request.getIssueNumber().trim();
            if (!newBarcode.equals(book.getCustomBarcode()) && bookRepository.existsByCustomBarcode(newBarcode)) {
                throw new ResourceAlreadyExistsException("Custom barcode already exists: " + newBarcode);
            }
            book.setIssueNumber(request.getIssueNumber().trim());
            book.setCustomBarcode(newBarcode);
        }

        book = bookRepository.save(book);

        if (request.getSeriesIds() != null) {
            bookSeriesMappingRepository.deleteByBookId(book.getId());
            if (!request.getSeriesIds().isEmpty()) {
                attachSeries(book, request.getSeriesIds());
            }
        }

        return getBookById(book.getId());
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        if (book.isSerialized()) {
            List<Book> children = bookRepository.findByParentIdOrderByIssueNumberDesc(id);
            for (Book child : children) {
                bookSeriesMappingRepository.deleteByBookId(child.getId());
            }
            bookRepository.deleteAll(children);
        }

        bookSeriesMappingRepository.deleteByBookId(id);
        bookRepository.delete(book);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getChildren(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        return bookRepository.findByParentIdOrderByIssueNumberDesc(id).stream()
                .map(BookResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookInvoiceResponse> getBookInvoices(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book not found with id: " + bookId);
        }
        return bookInvoiceRepository.findByBookId(bookId).stream()
                .map(BookInvoiceResponse::from)
                .toList();
    }

    @Transactional
    public void bindInvoice(Long bookId, Long invoiceId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        if (bookInvoiceRepository.existsByBookIdAndInvoiceId(bookId, invoiceId)) {
            throw new ResourceAlreadyExistsException("Invoice already bound to this book");
        }

        BookInvoice binding = new BookInvoice();
        binding.setBook(book);
        binding.setInvoice(invoice);
        bookInvoiceRepository.save(binding);
    }

    @Transactional
    public void unbindInvoice(Long bookId, Long invoiceId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book not found with id: " + bookId);
        }
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + invoiceId);
        }
        bookInvoiceRepository.deleteByBookIdAndInvoiceId(bookId, invoiceId);
    }

    @Transactional(readOnly = true)
    public List<BookPictureResponse> getBookPictures(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book not found with id: " + bookId);
        }
        return bookPictureRepository.findByBookId(bookId).stream()
                .map(BookPictureResponse::from)
                .toList();
    }

    @Transactional
    public BookPictureResponse uploadPicture(Long bookId, Long fileId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        FileRecord file = fileRecordRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + fileId));

        BookPicture picture = new BookPicture();
        picture.setBook(book);
        picture.setFile(file);
        return BookPictureResponse.from(bookPictureRepository.save(picture));
    }

    @Transactional
    public void deletePicture(Long bookId, Long pictureId) {
        BookPicture picture = bookPictureRepository.findById(pictureId)
                .orElseThrow(() -> new ResourceNotFoundException("Picture not found with id: " + pictureId));
        if (!picture.getBook().getId().equals(bookId)) {
            throw new ResourceNotFoundException("Picture not found for this book");
        }
        bookPictureRepository.delete(picture);
    }

    @Transactional(readOnly = true)
    public List<BookSeriesResponse> getBookSeriesList(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book not found with id: " + bookId);
        }
        return bookSeriesMappingRepository.findByBookId(bookId).stream()
                .map(m -> BookSeriesResponse.from(m.getSeries()))
                .toList();
    }

    @Transactional
    public List<BookSeriesResponse> setBookSeries(Long bookId, List<Long> seriesIds) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        bookSeriesMappingRepository.deleteByBookId(bookId);

        if (seriesIds != null && !seriesIds.isEmpty()) {
            attachSeries(book, seriesIds);
        }

        return bookSeriesMappingRepository.findByBookId(bookId).stream()
                .map(m -> BookSeriesResponse.from(m.getSeries()))
                .toList();
    }

    private void attachSeries(Book book, List<Long> seriesIds) {
        for (Long seriesId : seriesIds) {
            BookSeries series = bookSeriesRepository.findById(seriesId)
                    .orElseThrow(() -> new ResourceNotFoundException("Book series not found with id: " + seriesId));
            if (!bookSeriesMappingRepository.existsByBookIdAndSeriesId(book.getId(), seriesId)) {
                BookSeriesMapping mapping = new BookSeriesMapping();
                mapping.setBook(book);
                mapping.setSeries(series);
                bookSeriesMappingRepository.save(mapping);
            }
        }
    }

    private String generateBarcode(BookCategory category) {
        String prefix = category.getKey();
        long maxSeq = bookRepository.findMaxSequenceForPrefix(prefix);
        return prefix + String.format("%05d", maxSeq + 1);
    }

    private String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
