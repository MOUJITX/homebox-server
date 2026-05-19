package com.moujitx.homebox.server.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class MatchInfo {

    private Long chunkId;
    private Integer page;
    private String snippet;
    private List<String> matchTerms;

    public MatchInfo(Long chunkId, Integer page, String snippet, List<String> matchTerms) {
        this.chunkId = chunkId;
        this.page = page;
        this.snippet = snippet;
        this.matchTerms = matchTerms;
    }
}
