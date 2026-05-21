package com.moujitx.homebox.server.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.moujitx.homebox.server.event.ConfigChangedEvent;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EsClientProvider {

    private final ElasticsearchClient elasticsearchClient;
    private final SystemConfigService systemConfigService;

    public EsClientProvider(@Autowired(required = false) ElasticsearchClient elasticsearchClient,
                            SystemConfigService systemConfigService) {
        this.elasticsearchClient = elasticsearchClient;
        this.systemConfigService = systemConfigService;
    }

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
        if (!enabled || elasticsearchClient == null) return false;
        try {
            elasticsearchClient.ping();
            return true;
        } catch (Exception e) {
            log.warn("Elasticsearch ping failed: {}", e.getMessage());
            return false;
        }
    }
}
