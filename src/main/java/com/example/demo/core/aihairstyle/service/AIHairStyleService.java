package com.example.demo.core.aihairstyle.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIHairStyleService {

    @Value("${replicate.api.key}")
    private String replicateApiKey;

    @Value("${cloudinary.cloud.name}")
    private String cloudinaryCloudName;

    @Value("${cloudinary.api.key}")
    private String cloudinaryApiKey;

    @Value("${cloudinary.api.secret}")
    private String cloudinaryApiSecret;

    private final HairStyleSuggestionRepository suggestionRepository;
    private final HairStyleTemplateRepository templateRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Upload ảnh lên Cloudinary
     */
    public String uploadImage(MultipartFile file) throws Exception {
        String uploadUrl = "https://api.cloudinary.com/v1_1/" + cloudinaryCloudName + "/image/upload";

        // Prepare request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Convert file to base64
        String base64Image = "data:image/jpeg;base64," +
                Base64.getEncoder().encodeToString(file.getBytes());

        Map<String, String> body = new HashMap<>();
        body.put("file", base64Image);
        body.put("upload_preset", "hair_styles"); // Tạo preset này trên Cloudinary

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(uploadUrl, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return (String) response.getBody().get("secure_url");
        }

        throw new RuntimeException("Failed to upload image");
    }

    /**
     * Phân tích khuôn mặt bằng AI
     */
    public Map<String, Object> analyzeFace(String imageUrl) {
        String apiUrl = "https://api.replicate.com/v1/predictions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(replicateApiKey);

        Map<String, Object> input = new HashMap<>();
        input.put("image", imageUrl);

        Map<String, Object> body = new HashMap<>();
        body.put("version", "face-detection-model-version"); // Replace with actual model
        body.put("input", input);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

        return response.getBody();
    }

    /**
     * Generate preview kiểu tóc bằng AI
     */
    public String generateHairStylePreview(String faceImageUrl, String styleImageUrl) {
        String apiUrl = "https://api.replicate.com/v1/predictions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(replicateApiKey);

        Map<String, Object> input = new HashMap<>();
        input.put("source_image", faceImageUrl);
        input.put("style_image", styleImageUrl);
        input.put("prompt", "professional hair style transfer, realistic, high quality");

        Map<String, Object> body = new HashMap<>();
        // Sử dụng model phù hợp: InstantID, FaceSwap, hoặc Stable Diffusion
        body.put("version", "stability-ai/sdxl:39ed52f2a78e934b3ba6e2a89f5b1c712de7dfea535525255b1aa35c5565e08b");
        body.put("input", input);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
            Map<String, Object> result = response.getBody();

            // Get prediction ID
            String predictionId = (String) result.get("id");

            // Poll for result (AI generation takes time)
            return pollForResult(predictionId);

        } catch (Exception e) {
            log.error("Error generating hair style preview", e);
            throw new RuntimeException("Failed to generate preview: " + e.getMessage());
        }
    }

    /**
     * Poll Replicate API cho kết quả
     */
    private String pollForResult(String predictionId) throws InterruptedException {
        String statusUrl = "https://api.replicate.com/v1/predictions/" + predictionId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(replicateApiKey);
        HttpEntity<?> request = new HttpEntity<>(headers);

        for (int i = 0; i < 60; i++) { // Poll for 5 minutes max
            Thread.sleep(5000); // Wait 5 seconds

            ResponseEntity<Map> response = restTemplate.exchange(
                    statusUrl, HttpMethod.GET, request, Map.class
            );

            Map<String, Object> result = response.getBody();
            String status = (String) result.get("status");

            if ("succeeded".equals(status)) {
                List<String> output = (List<String>) result.get("output");
                return output.get(0); // URL của ảnh kết quả
            } else if ("failed".equals(status)) {
                throw new RuntimeException("AI generation failed");
            }
        }

        throw new RuntimeException("AI generation timeout");
    }

    /**
     * Tạo gợi ý kiểu tóc hoàn chỉnh
     */
    public HairStyleSuggestion createSuggestion(
            Long userId,
            MultipartFile faceImage,
            Long templateId
    ) throws Exception {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        HairStyleTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        // 1. Upload ảnh khuôn mặt
        log.info("Uploading face image...");
        String faceImageUrl = uploadImage(faceImage);

        // 2. Phân tích khuôn mặt
        log.info("Analyzing face...");
        Map<String, Object> faceAnalysis = analyzeFace(faceImageUrl);

        // 3. Generate preview
        log.info("Generating hair style preview...");
        String resultImageUrl = generateHairStylePreview(faceImageUrl, template.getImageUrl());

        // 4. Lưu kết quả
        HairStyleSuggestion suggestion = new HairStyleSuggestion();
        suggestion.setUser(user);
        suggestion.setOriginalImageUrl(faceImageUrl);
        suggestion.setStyleImageUrl(template.getImageUrl());
        suggestion.setResultImageUrl(resultImageUrl);
        suggestion.setFaceShape((String) faceAnalysis.getOrDefault("face_shape", "oval"));
        suggestion.setSkinTone((String) faceAnalysis.getOrDefault("skin_tone", "medium"));
        suggestion.setHairTexture(template.getLength());
        suggestion.setHairLength(template.getLength());
        suggestion.setAiAnalysis("AI-generated hair style preview based on face analysis");
        suggestion.setConfidenceScore(85);
        suggestion.setCreatedAt(LocalDateTime.now());

        return suggestionRepository.save(suggestion);
    }

    /**
     * Lấy danh sách template
     */
    public List<HairStyleTemplate> getTemplates(String gender, String length) {
        if (gender != null && length != null) {
            return templateRepository.findByGenderAndLengthAndIsActiveTrue(gender, length);
        } else if (gender != null) {
            return templateRepository.findByGenderAndIsActiveTrue(gender);
        }
        return templateRepository.findByIsActiveTrue();
    }
}
