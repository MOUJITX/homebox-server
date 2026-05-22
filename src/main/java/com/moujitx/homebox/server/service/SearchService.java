package com.moujitx.homebox.server.service;

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.util.NamedValue;
import com.moujitx.homebox.server.dto.response.MatchInfo;
import com.moujitx.homebox.server.dto.response.SearchResultItem;
import com.moujitx.homebox.server.dto.response.SourceInfo;
import com.moujitx.homebox.server.entity.AssetAttachment;
import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.entity.GoodAttachment;
import com.moujitx.homebox.server.entity.Invoice;
import com.moujitx.homebox.server.entity.InvoiceAttachment;
import com.moujitx.homebox.server.entity.SubscriptionRecordAttachment;
import com.moujitx.homebox.server.enums.SourceType;
import com.moujitx.homebox.server.repository.AssetAttachmentRepository;
import com.moujitx.homebox.server.repository.FileRecordRepository;
import com.moujitx.homebox.server.repository.GoodAttachmentRepository;
import com.moujitx.homebox.server.repository.InvoiceAttachmentRepository;
import com.moujitx.homebox.server.repository.InvoiceRepository;
import com.moujitx.homebox.server.repository.SubscriptionRecordAttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private static final int MAX_MATCHES_PER_FILE = 5;

    private final EsClientProvider esClientProvider;
    private final FileRecordRepository fileRecordRepository;
    private final AssetAttachmentRepository assetAttachmentRepository;
    private final GoodAttachmentRepository goodAttachmentRepository;
    private final InvoiceAttachmentRepository invoiceAttachmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRecordAttachmentRepository subscriptionRecordAttachmentRepository;

    public Page<SearchResultItem> search(String q, int page, int size) {
        if (q == null || q.isBlank()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), 0);
        }

        if (!esClientProvider.isSearchEnabled()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), 0);
        }
        var client = esClientProvider.getClient();

        try {
            int esSize = page * size * MAX_MATCHES_PER_FILE + size * MAX_MATCHES_PER_FILE;

            SearchResponse<Map> esResponse = client.search(
                    SearchRequest.of(s -> s
                            .index("chunks")
                            .query(qq -> qq
                                    .multiMatch(mm -> mm
                                            .fields("chunkText")
                                            .query(q)
                                            .type(TextQueryType.BestFields)
                                    )
                            )
                            .highlight(h -> h
                                    .fields(NamedValue.of("chunkText", HighlightField.of(hf -> hf
                                            .fragmentSize(100)
                                            .numberOfFragments(5)
                                            .preTags("<mark>")
                                            .postTags("</mark>")
                                    )))
                            )
                            .size(esSize)
                    ),
                    Map.class
            );

            List<SearchResultItem> items = buildResultItems(esResponse, q);
            int totalElements = items.size();

            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, items.size());
            List<SearchResultItem> pageContent = fromIndex < items.size()
                    ? items.subList(fromIndex, toIndex)
                    : List.of();

            return new PageImpl<>(pageContent, PageRequest.of(page, size), totalElements);
        } catch (Exception e) {
            log.warn("Search failed for query '{}': {}", q, e.getMessage());
            return new PageImpl<>(List.of(), PageRequest.of(page, size), 0);
        }
    }

    private List<SearchResultItem> buildResultItems(SearchResponse<Map> esResponse, String query) {
        List<Hit<Map>> hits = esResponse.hits().hits();
        if (hits.isEmpty()) return List.of();

        Map<Long, List<Hit<Map>>> groupedByFileId = new LinkedHashMap<>();
        for (Hit<Map> hit : hits) {
            Long fileId = getFileId(hit);
            if (fileId != null) {
                groupedByFileId.computeIfAbsent(fileId, k -> new ArrayList<>()).add(hit);
            }
        }

        if (groupedByFileId.isEmpty()) return List.of();

        List<Long> fileIds = new ArrayList<>(groupedByFileId.keySet());
        Map<Long, FileRecord> fileRecordMap = fileRecordRepository.findByIdIn(fileIds).stream()
                .collect(Collectors.toMap(FileRecord::getId, f -> f));
        Map<Long, List<SourceInfo>> sourceMap = buildSourceMap(fileIds);

        List<SearchResultItem> results = new ArrayList<>();
        for (Map.Entry<Long, List<Hit<Map>>> entry : groupedByFileId.entrySet()) {
            Long fileId = entry.getKey();
            List<Hit<Map>> fileHits = entry.getValue();

            FileRecord file = fileRecordMap.get(fileId);
            if (file == null) continue;

            fileHits.sort(Comparator.comparingDouble((Hit<Map> hit) -> hit.score()).reversed());
            List<Hit<Map>> topHits = fileHits.subList(0, Math.min(fileHits.size(), MAX_MATCHES_PER_FILE));

            List<MatchInfo> matches = topHits.stream()
                    .map(hit -> buildMatchInfo(hit, query))
                    .filter(Objects::nonNull)
                    .toList();

            double maxScore = topHits.stream().mapToDouble(hit -> hit.score()).max().orElse(0);

            List<SourceInfo> sources = sourceMap.getOrDefault(fileId, List.of());
            if (sources.isEmpty()) {
                sources = List.of(new SourceInfo(SourceType.FILE, null, null, null));
            }

            results.add(new SearchResultItem(
                    fileId,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getFileSize(),
                    sources,
                    matches,
                    maxScore
            ));
        }

        results.sort(Comparator.comparingDouble(SearchResultItem::getScore).reversed());
        return results;
    }

    private Long getFileId(Hit<Map> hit) {
        Object fileIdObj = hit.source() != null ? hit.source().get("fileId") : null;
        if (fileIdObj instanceof Number) {
            return ((Number) fileIdObj).longValue();
        }
        return null;
    }

    private MatchInfo buildMatchInfo(Hit<Map> hit, String query) {
        Long chunkId = null;
        Object chunkIdObj = hit.source() != null ? hit.source().get("chunkId") : null;
        if (chunkIdObj instanceof Number) {
            chunkId = ((Number) chunkIdObj).longValue();
        }

        Integer pageNumber = null;
        Object pageObj = hit.source() != null ? hit.source().get("pageNumber") : null;
        if (pageObj instanceof Number) {
            pageNumber = ((Number) pageObj).intValue();
        }

        String snippet = "";
        List<String> highlightList = null;
        if (hit.highlight() != null) {
            highlightList = hit.highlight().get("chunkText");
        }
        if (highlightList != null && !highlightList.isEmpty()) {
            snippet = highlightList.get(0);
        } else if (hit.source() != null && hit.source().get("chunkText") != null) {
            String text = hit.source().get("chunkText").toString();
            snippet = text.length() > 100 ? text.substring(0, 100) + "..." : text;
        }

        List<String> matchTerms = extractMatchTerms(query);

        return new MatchInfo(chunkId, pageNumber, snippet, matchTerms);
    }

    private List<String> extractMatchTerms(String query) {
        if (query == null || query.isBlank()) return List.of();
        return Arrays.stream(query.split("\\s+"))
                .filter(s -> !s.isBlank())
                .toList();
    }

    private Map<Long, List<SourceInfo>> buildSourceMap(List<Long> fileIds) {
        Map<Long, List<SourceInfo>> map = new HashMap<>();

        List<AssetAttachment> assetAtts = assetAttachmentRepository.findByFileIdIn(fileIds);
        List<GoodAttachment> goodAtts = goodAttachmentRepository.findByFileIdIn(fileIds);
        List<InvoiceAttachment> invoiceAtts = invoiceAttachmentRepository.findByFileIdIn(fileIds);
        List<Invoice> primaryInvoices = invoiceRepository.findByFileIdIn(fileIds);

        for (AssetAttachment aa : assetAtts) {
            Long fileId = aa.getFile().getId();
            SourceInfo source = new SourceInfo(
                    SourceType.ASSET,
                    "资产",
                    aa.getAsset().getId(),
                    aa.getAsset().getName()
            );
            map.computeIfAbsent(fileId, k -> new ArrayList<>()).add(source);
        }

        for (GoodAttachment ga : goodAtts) {
            Long fileId = ga.getFile().getId();
            SourceInfo source = new SourceInfo(
                    SourceType.GOOD,
                    "有效期",
                    ga.getGood().getId(),
                    ga.getGood().getProductName()
            );
            map.computeIfAbsent(fileId, k -> new ArrayList<>()).add(source);
        }

        for (InvoiceAttachment ia : invoiceAtts) {
            Long fileId = ia.getFile().getId();
            Invoice invoice = ia.getInvoice();
            SourceInfo source = new SourceInfo(
                    SourceType.INVOICE,
                    "发票",
                    invoice.getId(),
                    invoice.getInvoiceNumber()
            );
            map.computeIfAbsent(fileId, k -> new ArrayList<>()).add(source);
        }

        List<SubscriptionRecordAttachment> subAtts = subscriptionRecordAttachmentRepository.findByFileIdIn(fileIds);

        for (SubscriptionRecordAttachment sa : subAtts) {
            Long fileId = sa.getFile().getId();
            SourceInfo source = new SourceInfo(
                    SourceType.SUBSCRIPTION,
                    "订阅",
                    sa.getRecord().getSubscription().getId(),
                    sa.getRecord().getSubscription().getName()
            );
            map.computeIfAbsent(fileId, k -> new ArrayList<>()).add(source);
        }

        for (Invoice inv : primaryInvoices) {
            Long fileId = inv.getFile().getId();
            if (fileId == null) continue;

            Long invoiceId = inv.getId();
            List<SourceInfo> existing = map.get(fileId);
            boolean alreadyAdded = existing != null && existing.stream().anyMatch(
                    s -> s.getType() == SourceType.INVOICE && invoiceId.equals(s.getSourceId()));
            if (alreadyAdded) continue;

            SourceInfo source = new SourceInfo(
                    SourceType.INVOICE,
                    "发票",
                    invoiceId,
                    inv.getInvoiceNumber()
            );
            map.computeIfAbsent(fileId, k -> new ArrayList<>()).add(source);
        }

        return map;
    }
}
