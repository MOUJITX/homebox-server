package com.moujitx.homebox.server.service;

import tools.jackson.databind.ObjectMapper;
import com.moujitx.homebox.server.dto.response.InvoiceParseResponse;
import com.moujitx.homebox.server.dto.response.TestConnectionResponse;
import com.moujitx.homebox.server.dto.response.VisitRecordParseResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger aiLog = LoggerFactory.getLogger("com.moujitx.homebox.server.ai");

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

    private static final String VISIT_RECORD_PROMPT = """
            你是一个就诊记录信息提取助手。请从给定的文本中提取以下信息，并以JSON格式返回。

            字段说明：
            - patientName: 就诊人姓名
            - patientAge: 年龄（数字）
            - patientGender: 性别，取值为 MALE 或 FEMALE
            - visitType: 就诊类型，取值为 OUTPATIENT 或 INPATIENT。如果文本提到住院、入院、出院等，则为 INPATIENT；如果提到门诊、挂号等，则为 OUTPATIENT
            - visitDate: 就诊日期（门诊）或入院日期（住院），格式为 yyyy-MM-dd
            - medicalContent: 病历内容摘要
            - diagnosis: 诊断结果或诊断结论
            - doctor: 医生姓名
            - department: 就诊科室（门诊）或入院科室（住院）
            - dischargeDate: 出院时间（仅住院），格式为 yyyy-MM-dd
            - dischargeDept: 出院科室（仅住院）

            要求：
            1. 仅返回JSON对象，不要包含其他文字说明或markdown代码块标记
            2. 如果某个字段无法从文本中提取，设为null
            3. 日期格式必须为 yyyy-MM-dd
            4. 年龄必须是整数数字或null
            """;

    @SuppressWarnings("unchecked")
    private Map<String, String> resolveActiveModel() {
        String modelsJson = systemConfigService.get("ai.models");
        String activeModelId = systemConfigService.get("ai.active-model");

        if (modelsJson != null && !modelsJson.isBlank() && activeModelId != null && !activeModelId.isBlank()) {
            try {
                List<Map<String, Object>> models = objectMapper.readValue(modelsJson, List.class);
                for (Map<String, Object> m : models) {
                    if (activeModelId.equals(m.get("id"))) {
                        return Map.of(
                                "apiUrl", String.valueOf(m.get("apiUrl")),
                                "apiKey", String.valueOf(m.get("apiKey")),
                                "model", String.valueOf(m.get("model")));
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse ai.models JSON", e);
            }
        }

        return null;
    }

    public InvoiceParseResponse extractInvoiceInfo(String text) {
        Map<String, String> activeModel = resolveActiveModel();
        if (activeModel == null) {
            log.warn("No active AI model configured, skipping AI extraction");
            return new InvoiceParseResponse();
        }

        String apiUrl = activeModel.get("apiUrl");
        String apiKey = activeModel.get("apiKey");
        String model = activeModel.get("model");
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

            aiLog.info("AI request to {} with model={}. System prompt: {}. User content: {}",
                    apiUrl, model, systemPrompt, text);

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

    public VisitRecordParseResponse extractVisitRecordInfo(String text) {
        Map<String, String> activeModel = resolveActiveModel();
        if (activeModel == null) {
            log.warn("No active AI model configured, skipping AI extraction");
            return null;
        }

        String apiUrl = activeModel.get("apiUrl");
        String apiKey = activeModel.get("apiKey");
        String model = activeModel.get("model");
        String systemPrompt = systemConfigService.get("ai.visit-record-prompt");
        if (systemPrompt == null || systemPrompt.isBlank()) {
            systemPrompt = VISIT_RECORD_PROMPT;
        }

        if (apiUrl == null || apiUrl.isBlank()) {
            log.warn("AI API URL is not configured");
            return null;
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
                return null;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices.isEmpty()) {
                return null;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");
            if (content == null || content.isBlank()) {
                return null;
            }

            content = content.strip();
            if (content.startsWith("```")) {
                content = content.replaceFirst("```(?:json)?\\s*", "");
                content = content.replaceFirst("\\s*```$", "");
            }

            return objectMapper.readValue(content, VisitRecordParseResponse.class);
        } catch (Exception e) {
            log.error("AI visit record extraction failed", e);
            return null;
        }
    }

    public TestConnectionResponse testConnection() {
        Map<String, String> activeModel = resolveActiveModel();
        if (activeModel == null) {
            return new TestConnectionResponse(false, "No active AI model configured");
        }

        String apiUrl = activeModel.get("apiUrl");
        String apiKey = activeModel.get("apiKey");
        String model = activeModel.get("model");

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
