package com.moujitx.homebox.server.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.moujitx.homebox.server.entity.TextChunk;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EsIndexService {

    private static final String INDEX_NAME = "chunks";

    private final ElasticsearchClient esClient;

    @PostConstruct
    public void init() {
        try {
            createIndexIfNotExists();
            log.info("Elasticsearch connection established, index ready");
        } catch (Exception e) {
            log.warn("Elasticsearch unavailable, search features will be disabled: {}", e.getMessage());
        }
    }

    public void createIndexIfNotExists() {
        try {
            boolean exists = esClient.indices().exists(ExistsRequest.of(e -> e.index(INDEX_NAME))).value();
            if (!exists) {
                esClient.indices().create(CreateIndexRequest.of(c -> c
                        .index(INDEX_NAME)
                        .settings(s -> s
                                .numberOfShards("1")
                                .numberOfReplicas("0")
                                .refreshInterval(ri -> ri.time("1s"))
                                .analysis(a -> a
                                        .analyzer("ik_smart", analyzer -> analyzer
                                                .custom(custom -> custom.tokenizer("ik_smart")))
                                )
                        )
                        .mappings(TypeMapping.of(m -> m
                                .properties("chunkId", p -> p.long_(lp -> lp))
                                .properties("fileId", p -> p.long_(lp -> lp))
                                .properties("chunkIndex", p -> p.integer_(ip -> ip))
                                .properties("chunkText", p -> p.text(t -> t
                                        .analyzer("ik_max_word")
                                        .searchAnalyzer("ik_smart")))
                                .properties("pageNumber", p -> p.integer_(ip -> ip))
                                .properties("createdAt", p -> p.date(d -> d))
                        ))
                ));
                log.info("Created ES index: {}", INDEX_NAME);
            }
        } catch (Exception e) {
            log.warn("Failed to create ES index {}: {}", INDEX_NAME, e.getMessage());
        }
    }

    public void indexChunks(List<TextChunk> chunks) {
        if (chunks.isEmpty()) return;

        try {
            List<BulkOperation> operations = new ArrayList<>();
            for (TextChunk chunk : chunks) {
                Map<String, Object> doc = new HashMap<>();
                doc.put("chunkId", chunk.getId());
                doc.put("fileId", chunk.getFileId());
                doc.put("chunkIndex", chunk.getChunkIndex());
                doc.put("chunkText", chunk.getChunkText());
                if (chunk.getPageNumber() != null) {
                    doc.put("pageNumber", chunk.getPageNumber());
                }
                if (chunk.getCreatedAt() != null) {
                    doc.put("createdAt", chunk.getCreatedAt().toString());
                }
                operations.add(BulkOperation.of(op -> op
                        .index(idx -> idx
                                .index(INDEX_NAME)
                                .id(String.valueOf(chunk.getId()))
                                .document(doc))));
            }

            esClient.bulk(BulkRequest.of(b -> b.operations(operations)));
            log.debug("Indexed {} chunks to ES", chunks.size());
        } catch (Exception e) {
            log.warn("Failed to index {} chunks to ES: {}", chunks.size(), e.getMessage());
        }
    }

    public void deleteByFileId(Long fileId) {
        try {
            esClient.deleteByQuery(DeleteByQueryRequest.of(d -> d
                    .index(INDEX_NAME)
                    .query(q -> q.term(t -> t.field("fileId").value(fileId)))
            ));
        } catch (Exception e) {
            log.warn("Failed to delete ES docs for fileId {}: {}", fileId, e.getMessage());
        }
    }
}
