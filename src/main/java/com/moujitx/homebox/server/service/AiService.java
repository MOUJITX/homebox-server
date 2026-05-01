package com.moujitx.homebox.server.service;

import tools.jackson.databind.ObjectMapper;
import com.moujitx.homebox.server.dto.response.InvoiceParseResponse;
import com.moujitx.homebox.server.dto.response.TestConnectionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SystemConfigService systemConfigService;

    public AiService(@Qualifier("aiRestTemplate") RestTemplate restTemplate,
                     ObjectMapper objectMapper,
                     SystemConfigService systemConfigService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.systemConfigService = systemConfigService;
    }

    private static final String SYSTEM_PROMPT = """
            你是一个发票信息提取助手。请从给定的发票文本中提取以下信息，并以JSON格式返回。

            字段说明：
            - invoiceNumber: 发票号码
            - invoiceDate: 开票日期，格式为 yyyy-MM-dd
            - invoiceType: 发票类型，取值为以下之一：
            DIGITAL_INVOICE, RAILWAY_ELECTRONIC, VAT_INVOICE, AIR_ELECTRONIC, GENERAL_MACHINE_PRINTED, QUOTA_INVOICE, 
            NON_TAX_INCOME_GENERAL, NON_TAX_INCOME_UNIFIED, FUND_SETTLEMENT, MEDICAL_OUTPATIENT, MEDICAL_INPATIENT,OTHER
            - invoiceStatus: 发票状态，取值为以下之一：NORMAL, VOIDED, RED_FLUSHED
            - sellerName: 销售方名称
            - sellerTaxId: 销售方纳税人识别号
            - buyerName: 购买方名称
            - buyerTaxId: 购买方纳税人识别号
            - amount: 不含税金额
            - taxAmount: 税额
            - totalAmount: 价税合计
            - remark: 备注

            要求：
            1. 仅返回JSON对象，不要包含其他文字说明或markdown代码块标记
            2. 如果某个字段无法从文本中提取，设为null
            3. 金额字段使用数字格式，不包含货币符号和千分位分隔符
            4. 日期格式必须为 yyyy-MM-dd
            """;

    public InvoiceParseResponse extractInvoiceInfo(String text) {
        String apiUrl = systemConfigService.get("ai.api-url");
        String apiKey = systemConfigService.get("ai.api-key");
        String model = systemConfigService.get("ai.model");
        String systemPrompt = systemConfigService.get("ai.system-prompt");
        if (systemPrompt == null || systemPrompt.isBlank()) {
            systemPrompt = SYSTEM_PROMPT;
        }

        if (apiUrl == null || apiUrl.isBlank()) {
            log.warn("AI API URL is not configured, skipping AI extraction");
            return new InvoiceParseResponse();
        }

        String endpoint = apiUrl;
        if (!endpoint.endsWith("/chat/completions")) {
            endpoint = endpoint.replaceAll("/+$", "") + "/chat/completions";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", text)),
                    "temperature", 0.1);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(endpoint, request, Map.class);

            if (response == null || !response.containsKey("choices")) {
                log.warn("Unexpected AI API response: {}", response);
                return new InvoiceParseResponse();
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices.isEmpty()) {
                return new InvoiceParseResponse();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");
            if (content == null || content.isBlank()) {
                return new InvoiceParseResponse();
            }

            content = content.strip();
            // Strip markdown code block markers if present
            if (content.startsWith("```")) {
                content = content.replaceFirst("```(?:json)?\\s*", "");
                content = content.replaceFirst("\\s*```$", "");
            }

            return objectMapper.readValue(content, InvoiceParseResponse.class);
        } catch (Exception e) {
            log.error("AI extraction failed", e);
            return new InvoiceParseResponse();
        }
    }

    public TestConnectionResponse testConnection() {
        String apiUrl = systemConfigService.get("ai.api-url");
        String apiKey = systemConfigService.get("ai.api-key");
        String model = systemConfigService.get("ai.model");

        if (apiUrl == null || apiUrl.isBlank()) {
            return new TestConnectionResponse(false, "AI API URL is not configured");
        }

        String endpoint = apiUrl;
        if (!endpoint.endsWith("/chat/completions")) {
            endpoint = endpoint.replaceAll("/+$", "") + "/chat/completions";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "user", "content", "Say OK")),
                    "max_tokens", 5);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(endpoint, request, Map.class);

            if (response != null && response.containsKey("choices")) {
                return new TestConnectionResponse(true, "AI connection successful");
            }
            return new TestConnectionResponse(false, "Unexpected response from AI API");
        } catch (Exception e) {
            log.error("AI connection test failed", e);
            return new TestConnectionResponse(false, "AI connection failed: " + e.getMessage());
        }
    }
}
