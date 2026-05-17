package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final SystemConfigService systemConfigService;
    private final RestTemplate restTemplate = new RestTemplate();

    public void send(Notification notification) {
        try {
            doSend(notification);
        } catch (Exception e) {
            log.error("Failed to send webhook: {}", e.getMessage());
        }
    }

    public void sendTest(Notification notification) {
        doSend(notification);
    }

    private void doSend(Notification notification) {
        String enabled = systemConfigService.get("notification.webhook-enabled");
        if (!"true".equals(enabled)) {
            return;
        }

        String url = systemConfigService.get("notification.webhook-url");
        if (url.isEmpty()) {
            throw new RuntimeException("Webhook URL is not configured");
        }

        String template = systemConfigService.get("notification.webhook-template");
        if (template.isEmpty()) {
            template = getDefaultTemplate();
        }

        String payload = renderTemplate(template, notification);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForEntity(url, entity, String.class);
            log.info("Webhook sent to {}", url);
        } catch (Exception e) {
            String detail = e.getMessage();
            // Extract response body from RestClientException
            if (e instanceof org.springframework.web.client.HttpClientErrorException httpEx) {
                detail = httpEx.getResponseBodyAsString();
            }
            throw new RuntimeException("Webhook request failed: " + detail, e);
        }
    }

    private String getDefaultTemplate() {
        return """
                {
                  "type": "{{type}}",
                  "title": "{{title}}",
                  "content": "{{content}}",
                  "createdAt": "{{createdAt}}"
                }""";
    }

    String renderTemplate(String template, Notification notification) {
        String rendered = template;
        Map<String, String> placeholders = Map.of(
                "{{type}}", notification.getType().name(),
                "{{title}}", notification.getTitle(),
                "{{content}}", notification.getContent(),
                "{{createdAt}}", notification.getCreatedAt() != null
                        ? notification.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        : "",
                "{{appName}}", "Homebox"
        );
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            rendered = rendered.replace(entry.getKey(), entry.getValue());
        }
        return rendered;
    }
}
