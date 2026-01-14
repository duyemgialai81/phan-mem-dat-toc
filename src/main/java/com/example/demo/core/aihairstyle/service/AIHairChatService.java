package com.example.demo.core.aihairstyle.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIHairChatService {

    @Value("${python.ai.api.url:https://server-ai-li5o.onrender.com}")
    private String pythonAiApiUrl;

    private final RestTemplate restTemplate;

    /**
     * Chat với AI advisor về chăm sóc tóc
     */
    public Map<String, Object> chatWithAdvisor(String message, String hairType, List<Map<String, String>> history) {
        log.info("Chat with AI advisor - message: {}, hairType: {}", message, hairType);

        try {
            String apiUrl = pythonAiApiUrl + "/chat-advisor";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", message);
            requestBody.put("hair_type", hairType != null ? hairType : "normal");
            requestBody.put("history", history != null ? history : new ArrayList<>());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.info("Calling Python AI Chat API: {}", apiUrl);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, Object> result = response.getBody();
            if (result != null) {
                log.info("AI response received successfully");
                return result;
            } else {
                throw new RuntimeException("Empty response from AI");
            }

        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            log.error("HTTP error from Python AI Chat: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("response", "Xin lỗi, AI đang bận. Vui lòng thử lại sau.");
            errorResponse.put("error", true);
            errorResponse.put("model_loading", true);
            return errorResponse;

        } catch (Exception e) {
            log.error("Error calling AI chat advisor", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("response", "Xin lỗi, tôi không thể trả lời lúc này. Vui lòng thử lại sau ít phút.");
            errorResponse.put("error", true);
            return errorResponse;
        }
    }

    /**
     * Phân tích tình trạng tóc từ ảnh
     */
    public Map<String, Object> analyzeHairFromImage(String imageUrl) {
        log.info("Analyzing hair condition from image: {}", imageUrl);

        try {
            String apiUrl = pythonAiApiUrl + "/analyze-hair";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image_url", imageUrl);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.info("Calling Python AI Analyze API: {}", apiUrl);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, Object> result = response.getBody();
            if (result != null) {
                log.info("Hair analysis received successfully");
                return result;
            } else {
                throw new RuntimeException("Empty response from AI");
            }

        } catch (Exception e) {
            log.error("Error analyzing hair", e);

            // Fallback response
            Map<String, Object> fallbackResponse = new HashMap<>();
            fallbackResponse.put("hair_condition", "normal");
            fallbackResponse.put("recommendations", Arrays.asList(
                    "Sử dụng dầu gội phù hợp với loại tóc",
                    "Massage da đầu đều đặn",
                    "Tránh nhiệt độ cao khi sấy tóc"
            ));

            List<Map<String, String>> products = new ArrayList<>();
            Map<String, String> product1 = new HashMap<>();
            product1.put("name", "Dầu gội Tresemmé Keratin Smooth");
            product1.put("type", "shampoo");
            product1.put("where_to_buy", "Hasaki, Guardian, Watson's");
            product1.put("price_range", "150,000 - 200,000 VNĐ");
            products.add(product1);

            fallbackResponse.put("suggested_products", products);
            fallbackResponse.put("salon_services", Arrays.asList(
                    "Phục hồi tóc hư tổn",
                    "Cắt tỉa đuôi tóc"
            ));
            fallbackResponse.put("note", "Phân tích tự động. Nên tham khảo chuyên gia để có kết quả chính xác hơn.");

            return fallbackResponse;
        }
    }
}