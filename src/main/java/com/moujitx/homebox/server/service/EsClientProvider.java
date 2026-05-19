package com.moujitx.homebox.server.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.moujitx.homebox.server.event.ConfigChangedEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EsClientProvider {

    private final ElasticsearchClient elasticsearchClient;
    private final SystemConfigService systemConfigService;

    private volatile boolean enabled = false;

    @PostConstruct
    public void init() {
        refresh();
    }

    @EventListener
    public void onConfigChanged(ConfigChangedEvent event) {
        if ("elasticsearch".equals(event.getGroup())) {
            refresh();
        }
    }

    public synchronized void refresh() {
        String enabledStr = systemConfigService.get("elasticsearch.enabled");
        boolean wasEnabled = this.enabled;
        this.enabled = "true".equals(enabledStr);

        if (this.enabled && !wasEnabled) {
            log.info("Elasticsearch search enabled via system config");
        } else if (!this.enabled && wasEnabled) {
            log.info("Elasticsearch search disabled via system config");
        }
    }

    public ElasticsearchClient getClient() {
        return enabled ? elasticsearchClient : null;
    }

    public boolean isAvailable() {
        return enabled;
    }

    public boolean testConnection() {
        if (!enabled) return false;
        try {
            elasticsearchClient.ping();
            return true;
        } catch (Exception e) {
            log.warn("Elasticsearch ping failed: {}", e.getMessage());
            return false;
        }
    }
}
