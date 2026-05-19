package com.moujitx.homebox.server.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class SearchResultItem {

    private Long fileId;
    private String originalFilename;
    private String contentType;
    private long fileSize;
    private List<SourceInfo> sources;
    private List<MatchInfo> matches;
    private double score;

    public SearchResultItem(Long fileId, String originalFilename, String contentType, long fileSize,
                            List<SourceInfo> sources, List<MatchInfo> matches, double score) {
        this.fileId = fileId;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.sources = sources;
        this.matches = matches;
        this.score = score;
    }
}
