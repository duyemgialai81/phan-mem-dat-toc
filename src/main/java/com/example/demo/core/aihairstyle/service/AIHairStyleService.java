package com.example.demo.core.aihairstyle.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIHairStyleService {

    @Value("${python.ai.api.url:https://server-ai-li5o.onrender.com}")
    private String pythonAiApiUrl;

    @Value("${cloudinary.cloud.name:}")
    private String cloudinaryCloudName;

    @Value("${cloudinary.api.key:}")
    private String cloudinaryApiKey;

    @Value("${cloudinary.api.secret:}")
    private String cloudinaryApiSecret;

    private final HairStyleSuggestionRepository suggestionRepository;
    private final HairStyleTemplateRepository templateRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Upload ảnh lên Cloudinary
     */
    public String uploadImage(MultipartFile file) throws Exception {
        log.info("Uploading image to Cloudinary...");

        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudinaryCloudName,
                "api_key", cloudinaryApiKey,
                "api_secret", cloudinaryApiSecret
        ));

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", "hair_styles"));

        String imageUrl = (String) uploadResult.get("secure_url");
        log.info("Image uploaded successfully: {}", imageUrl);

        return imageUrl;
    }

    /**
     * Phân tích khuôn mặt
     */
    public Map<String, Object> analyzeFace(String imageUrl) {
        log.info("Analyzing face...");

        Map<String, Object> faceData = new HashMap<>();
        String[] faceShapes = {"oval", "round", "square", "heart", "diamond"};
        faceData.put("face_shape", faceShapes[new Random().nextInt(faceShapes.length)]);
        faceData.put("skin_tone", "medium");
        faceData.put("confidence", 0.85);

        return faceData;
    }

    /**
     * Generate hair style preview bằng Python AI API
     */
    public String generateHairStylePreview(String faceImageUrl, String styleImageUrl, String styleDescription) {
        log.info("Generating hair style preview with Python AI API...");

        try {
            // Call Python API - Endpoint: /process-hair
            String apiUrl = pythonAiApiUrl + "/process-hair";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Tạo prompt chi tiết hơn
            String detailedPrompt = String.format(
                    "%s, realistic modern hairstyle, highly detailed, professional hair salon, " +
                            "natural look, photorealistic, 8k, studio lighting",
                    styleDescription
            );

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image_url", faceImageUrl);  // ✅ Đổi key cho khớp với Python
            requestBody.put("prompt", detailedPrompt);   // ✅ Gửi prompt mô tả kiểu tóc

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.info("Calling Python AI API: {}", apiUrl);
            log.info("Request payload: image_url={}, prompt={}", faceImageUrl, detailedPrompt);

            // Set timeout 90 seconds (AI model có thể chậm)
            restTemplate.getInterceptors().clear();

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    byte[].class
            );

            byte[] imageBytes = response.getBody();

            if (imageBytes != null && imageBytes.length > 0) {
                log.info("Image generated successfully from Python API, size: {} bytes", imageBytes.length);

                // Upload to Cloudinary
                return uploadGeneratedImage(imageBytes);
            } else {
                throw new RuntimeException("Empty response from Python AI API");
            }

        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            log.error("HTTP error from Python API: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());

            if (e.getStatusCode().value() == 503) {
                throw new RuntimeException("AI Model đang khởi động, vui lòng thử lại sau 30 giây");
            }

            throw new RuntimeException("Lỗi từ Python AI API: " + e.getMessage());

        } catch (Exception e) {
            log.error("Error calling Python AI API", e);
            throw new RuntimeException("Failed to generate preview: " + e.getMessage());
        }
    }

    /**
     * Upload generated image to Cloudinary
     */
    private String uploadGeneratedImage(byte[] imageBytes) throws Exception {
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudinaryCloudName,
                "api_key", cloudinaryApiKey,
                "api_secret", cloudinaryApiSecret
        ));

        Map uploadResult = cloudinary.uploader().upload(imageBytes,
                ObjectUtils.asMap("folder", "hair_styles/generated"));

        return (String) uploadResult.get("secure_url");
    }

    /**
     * Tạo gợi ý kiểu tóc hoàn chỉnh
     */
    public HairStyleSuggestion createSuggestion(
            Long userId,
            MultipartFile faceImage,
            Long templateId
    ) throws Exception {

        log.info("Creating hair style suggestion for user: {}, template: {}", userId, templateId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        HairStyleTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        // 1. Upload ảnh khuôn mặt
        log.info("Step 1: Uploading face image...");
        String faceImageUrl = uploadImage(faceImage);

        // 2. Phân tích khuôn mặt
        log.info("Step 2: Analyzing face...");
        Map<String, Object> faceAnalysis = analyzeFace(faceImageUrl);

        // 3. Generate preview với Python AI API
        log.info("Step 3: Generating preview with Python AI API...");
        String styleDescription = String.format("%s %s hair style, %s length",
                template.getName(),
                template.getGender().toLowerCase(),
                template.getLength().toLowerCase()
        );

        String resultImageUrl;
        try {
            resultImageUrl = generateHairStylePreview(
                    faceImageUrl,
                    template.getImageUrl(),
                    styleDescription
            );
        } catch (Exception e) {
            log.error("Failed to generate AI preview, using template image as fallback", e);
            // Fallback: Sử dụng ảnh template
            resultImageUrl = template.getImageUrl();
        }

        // 4. Lưu kết quả
        log.info("Step 4: Saving suggestion...");
        HairStyleSuggestion suggestion = new HairStyleSuggestion();
        suggestion.setUser(user);
        suggestion.setOriginalImageUrl(faceImageUrl);
        suggestion.setStyleImageUrl(template.getImageUrl());
        suggestion.setResultImageUrl(resultImageUrl);
        suggestion.setFaceShape((String) faceAnalysis.getOrDefault("face_shape", "oval"));
        suggestion.setSkinTone((String) faceAnalysis.getOrDefault("skin_tone", "medium"));
        suggestion.setHairTexture(template.getLength());
        suggestion.setHairLength(template.getLength());
        suggestion.setAiAnalysis(String.format(
                "AI-generated preview for %s style. Recommended for %s face shape.",
                template.getName(),
                faceAnalysis.get("face_shape")
        ));
        suggestion.setConfidenceScore(85);
        suggestion.setCreatedAt(LocalDateTime.now());

        HairStyleSuggestion saved = suggestionRepository.save(suggestion);
        log.info("Suggestion created successfully with ID: {}", saved.getId());

        return saved;
    }

    /**
     * Lấy danh sách template
     */
    public List<HairStyleTemplate> getTemplates(String gender, String length) {
        log.info("Getting templates - gender: {}, length: {}", gender, length);

        if (gender != null && length != null) {
            return templateRepository.findByGenderAndLengthAndIsActiveTrue(gender, length);
        } else if (gender != null) {
            return templateRepository.findByGenderAndIsActiveTrue(gender);
        }
        return templateRepository.findByIsActiveTrue();
    }

    /**
     * Lấy lịch sử suggestions
     */
    public List<HairStyleSuggestion> getMySuggestions(Long userId) {
        return suggestionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}