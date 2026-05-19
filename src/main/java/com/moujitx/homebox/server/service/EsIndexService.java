package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.entity.TextChunk;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EsIndexService {

    private static final String INDEX_NAME = "chunks";

    private static final String INDEX_MAPPING = """
            {
              "settings": {
                "number_of_shards": 1,
                "number_of_replicas": 0,
                "refresh_interval": "1s",
                "analysis": {
                  "analyzer": {
                    "ik_smart": {
                      "type": "custom",
                      "tokenizer": "ik_smart"
                    }
                  }
                }
              },
              "mappings": {
                "properties": {
                  "chunkId":    { "type": "long" },
                  "fileId":     { "type": "long" },
                  "chunkIndex": { "type": "integer" },
                  "chunkText":  { "type": "text", "analyzer": "ik_max_word", "search_analyzer": "ik_smart" },
                  "pageNumber": { "type": "integer" },
                  "createdAt":  { "type": "date" }
                }
              }
            }""";

    private final co.elastic.clients.elasticsearch.ElasticsearchClient esClient;

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
            boolean exists = esClient.indices().exists(e -> e.index(INDEX_NAME)).value();
            if (!exists) {
                esClient.indices().create(c -> c
                        .index(INDEX_NAME)
                        .withJson(new StringReader(INDEX_MAPPING))
                );
                log.info("Created ES index: {}", INDEX_NAME);
            }
        } catch (Exception e) {
            log.warn("Failed to create ES index {}: {}", INDEX_NAME, e.getMessage());
        }
    }

    public void indexChunks(List<TextChunk> chunks) {
        if (chunks.isEmpty()) return;

        try {
            List<co.elastic.clients.elasticsearch.core.bulk.BulkOperation> operations = new ArrayList<>();
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
                operations.add(co.elastic.clients.elasticsearch.core.bulk.BulkOperation.of(op -> op
                        .index(idx -> idx
                                .index(INDEX_NAME)
                                .id(String.valueOf(chunk.getId()))
                                .document(doc))));
            }

            esClient.bulk(b -> b.operations(operations));
            log.debug("Indexed {} chunks to ES", chunks.size());
        } catch (Exception e) {
            log.warn("Failed to index {} chunks to ES: {}", chunks.size(), e.getMessage());
        }
    }

    public void deleteByFileId(Long fileId) {
        try {
            esClient.deleteByQuery(d -> d
                    .index(INDEX_NAME)
                    .query(q -> q.term(t -> t.field("fileId").value(fileId)))
            );
        } catch (Exception e) {
            log.warn("Failed to delete ES docs for fileId {}: {}", fileId, e.getMessage());
        }
    }
}
