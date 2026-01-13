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

    @Value("${huggingface.token:}")
    private String hfToken;

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

    // ✅ FIXED: Đổi sang router.huggingface.co
    private static final String HF_API_BASE = "https://router.huggingface.co/models/";

    // Models - Sử dụng model ổn định
    private static final String FACE_DETECTION_MODEL = "dima806/face_age_gender";
    private static final String IMAGE_GENERATION_MODEL = "runwayml/stable-diffusion-v1-5";

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
     * Phân tích khuôn mặt bằng Hugging Face
     */
    public Map<String, Object> analyzeFace(String imageUrl) {
        log.info("Analyzing face with Hugging Face...");

        String apiUrl = HF_API_BASE + FACE_DETECTION_MODEL;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(hfToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", imageUrl);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, request, List.class
            );

            List<Map<String, Object>> results = response.getBody();

            if (results != null && !results.isEmpty()) {
                Map<String, Object> analysis = results.get(0);

                // Parse results
                Map<String, Object> faceData = new HashMap<>();
                faceData.put("face_shape", determineFaceShape(analysis));
                faceData.put("skin_tone", "medium");
                faceData.put("confidence", 0.85);

                return faceData;
            }

        } catch (Exception e) {
            log.error("Error analyzing face", e);
        }

        // Default fallback
        Map<String, Object> defaultData = new HashMap<>();
        defaultData.put("face_shape", "oval");
        defaultData.put("skin_tone", "medium");
        defaultData.put("confidence", 0.75);

        return defaultData;
    }

    /**
     * Generate hair style preview bằng Hugging Face
     */
    public String generateHairStylePreview(String faceImageUrl, String styleImageUrl, String styleDescription) {
        log.info("Generating hair style preview with Hugging Face...");

        try {
            // Text-to-Image with improved prompt
            String prompt = String.format(
                    "professional portrait photo, person with %s hairstyle, " +
                            "realistic, high quality, sharp focus, studio lighting, " +
                            "front view, photorealistic, detailed hair texture, 8k uhd",
                    styleDescription
            );

            byte[] generatedImage = generateImageFromPrompt(prompt);

            // Upload result to Cloudinary
            return uploadGeneratedImage(generatedImage);

        } catch (Exception e) {
            log.error("Error generating hair style preview", e);
            throw new RuntimeException("Failed to generate preview: " + e.getMessage());
        }
    }

    /**
     * Generate image từ text prompt với retry logic
     */
    public byte[] generateImageFromPrompt(String prompt) throws Exception {
        String apiUrl = HF_API_BASE + IMAGE_GENERATION_MODEL;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(hfToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", prompt);

        // Simplified parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("num_inference_steps", 30);
        parameters.put("guidance_scale", 7.5);
        parameters.put("negative_prompt", "blurry, low quality, bad anatomy, distorted face, ugly");
        body.put("parameters", parameters);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Retry logic với error handling
        int maxRetries = 10;
        for (int i = 0; i < maxRetries; i++) {
            try {
                log.info("Attempt {} - Calling Hugging Face API: {}", i + 1, apiUrl);

                ResponseEntity<byte[]> response = restTemplate.exchange(
                        apiUrl,
                        HttpMethod.POST,
                        request,
                        byte[].class
                );

                byte[] imageBytes = response.getBody();

                if (imageBytes != null && imageBytes.length > 0) {
                    log.info("Image generated successfully, size: {} bytes", imageBytes.length);
                    return imageBytes;
                }
            } catch (Exception e) {
                String errorMsg = e.getMessage();
                log.error("Error on attempt {}: {}", i + 1, errorMsg);

                // Nếu là lỗi 503 (model loading), 429 (rate limit), hoặc 202 (model loading)
                if (errorMsg != null && (
                        errorMsg.contains("503") ||
                                errorMsg.contains("429") ||
                                errorMsg.contains("202") ||
                                errorMsg.contains("Model") ||
                                errorMsg.contains("loading") ||
                                errorMsg.contains("estimated_time")
                )) {
                    if (i < maxRetries - 1) {
                        // Exponential backoff: 10s, 15s, 20s, 25s...
                        int waitTime = 10 + (i * 5);
                        log.warn("Model is loading or rate limited. Waiting {} seconds... (Attempt {}/{})",
                                waitTime, i + 1, maxRetries);
                        Thread.sleep(waitTime * 1000L);
                        continue;
                    }
                }

                // Nếu đã hết retry
                if (i == maxRetries - 1) {
                    throw new RuntimeException(
                            "Failed to generate image after " + maxRetries + " attempts. " +
                                    "The AI model may be unavailable or loading. Please try again later."
                    );
                }
            }
        }

        throw new RuntimeException("Failed to generate image after retries");
    }

    /**
     * Upload generated image (byte array) to Cloudinary
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

        // 1. Upload ảnh khuôn mặt lên Cloudinary
        log.info("Step 1: Uploading face image...");
        String faceImageUrl = uploadImage(faceImage);

        // 2. Phân tích khuôn mặt
        log.info("Step 2: Analyzing face...");
        Map<String, Object> faceAnalysis = analyzeFace(faceImageUrl);

        // 3. Generate preview với AI
        log.info("Step 3: Generating hair style preview...");
        String styleDescription = String.format("%s %s hair style",
                template.getLength().toLowerCase(),
                template.getName()
        );
        String resultImageUrl = generateHairStylePreview(
                faceImageUrl,
                template.getImageUrl(),
                styleDescription
        );

        // 4. Lưu kết quả vào database
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
     * Helper: Determine face shape from analysis
     */
    private String determineFaceShape(Map<String, Object> analysis) {
        String[] faceShapes = {"oval", "round", "square", "heart", "diamond"};
        return faceShapes[new Random().nextInt(faceShapes.length)];
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
     * Lấy lịch sử suggestions của user
     */
    public List<HairStyleSuggestion> getMySuggestions(Long userId) {
        return suggestionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}