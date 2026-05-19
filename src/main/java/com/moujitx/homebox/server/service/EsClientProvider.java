package com.moujitx.homebox.server.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.moujitx.homebox.server.event.ConfigChangedEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EsClientProvider {

    private final SystemConfigService systemConfigService;

    private volatile ElasticsearchClient esClient;
    private volatile RestClient restClient;
    private volatile boolean available = false;

    @PostConstruct
    public void init() {
        refresh();
    }

    @PreDestroy
    public void destroy() {
        closeClient();
    }

    @EventListener
    public void onConfigChanged(ConfigChangedEvent event) {
        if ("elasticsearch".equals(event.getGroup())) {
            refresh();
        }
    }

    public synchronized void refresh() {
        closeClient();

        String enabled = systemConfigService.get("elasticsearch.enabled");
        if (!"true".equals(enabled)) {
            log.info("Elasticsearch is disabled via system config");
            this.available = false;
            this.esClient = null;
            return;
        }

        String host = systemConfigService.get("elasticsearch.host");
        String port = systemConfigService.get("elasticsearch.port");
        if (host.isEmpty()) host = "localhost";
        if (port.isEmpty()) port = "9200";

        try {
            int portNum = Integer.parseInt(port);
            this.restClient = RestClient.builder(new HttpHost(host, portNum, "http")).build();
            ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
            this.esClient = new ElasticsearchClient(transport);
            // Verify connectivity
            this.esClient.ping();
            this.available = true;
            log.info("Elasticsearch client connected to {}:{}", host, port);
        } catch (Exception e) {
            log.warn("Elasticsearch unavailable at {}:{} — search features disabled: {}", host, port, e.getMessage());
            this.available = false;
            this.esClient = null;
            closeRestClient();
        }
    }

    public ElasticsearchClient getClient() {
        return esClient;
    }

    public boolean isAvailable() {
        return available && esClient != null;
    }

    public boolean testConnection() {
        if (esClient == null) return false;
        try {
            esClient.ping();
            return true;
        } catch (Exception e) {
            log.warn("Elasticsearch ping failed: {}", e.getMessage());
            return false;
        }
    }

    private void closeClient() {
        if (esClient != null) {
            esClient = null;
            available = false;
        }
        closeRestClient();
    }

    private void closeRestClient() {
        if (restClient != null) {
            try {
                restClient.close();
            } catch (Exception e) {
                log.warn("Failed to close ES RestClient: {}", e.getMessage());
            }
            restClient = null;
        }
    }
}
