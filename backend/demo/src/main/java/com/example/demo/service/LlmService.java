package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LlmService {
    private final String baseUrl;
    private final String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();

    public LlmService(@Value("${app.llm.base-url}") String baseUrl,
                      @Value("${app.llm.api-key:}") String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public String chat(String model, List<Map<String, String>> messages) {
        if (apiKey == null || apiKey.isBlank()) {
            return "[LLM未配置API Key，返回占位回复]";
        }
        String url = baseUrl + "/chat/completions";
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> resp = restTemplate.postForEntity(url, request, Map.class);
        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            try {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        return String.valueOf(message.get("content"));
                    }
                }
            } catch (Exception ignored) {}
        }
        return "[LLM调用失败或无响应]";
    }
}
