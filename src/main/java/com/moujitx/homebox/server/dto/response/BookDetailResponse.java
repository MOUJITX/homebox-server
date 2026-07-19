package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Book;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class BookDetailResponse {

    Long id;
    String title;
    String author;
    String isbn;
    String customBarcode;
    boolean serialized;
    Long parentId;
    String parentName;
    String issueNumber;
    String publisher;
    LocalDate publishDate;
    String description;
    Long categoryId;
    String categoryName;
    Long locationId;
    String locationName;
    String status;
    LocalDate purchaseDate;
    BigDecimal purchasePrice;
    String note;
    List<BookPictureResponse> pictures;
    List<BookResponse> children;
    List<BookSeriesResponse> series;
    List<BookInvoiceResponse> invoices;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static BookDetailResponse from(Book book, List<BookResponse> children,
                                           List<BookSeriesResponse> series,
                                           List<BookInvoiceResponse> invoices) {
        BookDetailResponse r = new BookDetailResponse();
        r.id = book.getId();
        r.title = book.getTitle();
        r.author = book.getAuthor();
        r.isbn = book.getIsbn();
        r.customBarcode = book.getCustomBarcode();
        r.serialized = book.isSerialized();
        if (book.getParent() != null) {
            r.parentId = book.getParent().getId();
            r.parentName = book.getParent().getTitle();
        }
        r.issueNumber = book.getIssueNumber();
        r.publisher = book.getPublisher();
        r.publishDate = book.getPublishDate();
        r.description = book.getDescription();
        r.categoryId = book.getCategory().getId();
        r.categoryName = book.getCategory().getName();
        r.locationId = book.getLocation().getId();
        r.locationName = book.getLocation().getName();
        r.status = book.getStatus().name();
        r.purchaseDate = book.getPurchaseDate();
        r.purchasePrice = book.getPurchasePrice();
        r.note = book.getNote();
        if (book.getPictures() != null) {
            r.pictures = book.getPictures().stream()
                    .map(BookPictureResponse::from)
                    .toList();
        }
        r.children = children;
        r.series = series;
        r.invoices = invoices;
        r.createdAt = book.getCreatedAt();
        r.updatedAt = book.getUpdatedAt();
        return r;
    }
}
