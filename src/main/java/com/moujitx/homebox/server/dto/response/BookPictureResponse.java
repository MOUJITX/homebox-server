package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.BookPicture;
import lombok.Getter;

@Getter
public class BookPictureResponse {

    Long id;
    Long fileId;
    String filename;
    String contentType;
    Long fileSize;
    String url;

    public static BookPictureResponse from(BookPicture picture) {
        BookPictureResponse r = new BookPictureResponse();
        r.id = picture.getId();
        r.fileId = picture.getFile().getId();
        r.filename = picture.getFile().getFilename();
        r.contentType = picture.getFile().getContentType();
        r.fileSize = picture.getFile().getFileSize();
        r.url = "/api/files/" + picture.getFile().getId() + "/download";
        return r;
    }
}
