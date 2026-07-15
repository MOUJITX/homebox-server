package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Book;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class BookResponse {

    Long id;
    String title;
    String author;
    String isbn;
    String customBarcode;
    boolean serialized;
    Long parentId;
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
    String firstPictureUrl;
    boolean hasInvoice;
    int childCount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static BookResponse from(Book book) {
        BookResponse r = new BookResponse();
        r.id = book.getId();
        r.title = book.getTitle();
        r.author = book.getAuthor();
        r.isbn = book.getIsbn();
        r.customBarcode = book.getCustomBarcode();
        r.serialized = book.isSerialized();
        r.parentId = book.getParent() != null ? book.getParent().getId() : null;
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
        if (book.getPictures() != null && !book.getPictures().isEmpty()) {
            r.firstPictureUrl = "/api/files/" + book.getPictures().get(0).getFile().getId() + "/download";
        }
        r.hasInvoice = book.getInvoiceBindings() != null && !book.getInvoiceBindings().isEmpty();
        r.childCount = 0;
        r.createdAt = book.getCreatedAt();
        r.updatedAt = book.getUpdatedAt();
        return r;
    }
}
