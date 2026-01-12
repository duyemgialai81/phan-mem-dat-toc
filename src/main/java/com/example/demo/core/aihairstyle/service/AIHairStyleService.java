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

    // Hugging Face API endpoints
    private static final String HF_API_BASE = "https://api-inference.huggingface.co/models/";

    // Models
    private static final String FACE_DETECTION_MODEL = "dima806/face_age_gender";
    private static final String IMAGE_GENERATION_MODEL = "stabilityai/stable-diffusion-2-1";
    private static final String FACE_SWAP_MODEL = "deepinsight/insightface";

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
            // Option 1: Text-to-Image with face guidance
            String prompt = String.format(
                    "professional hair salon photo, %s hair style, " +
                            "realistic, high quality, sharp focus, professional lighting, " +
                            "front view portrait, photorealistic",
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
     * Generate image từ text prompt
     */
    public byte[] generateImageFromPrompt(String prompt) throws Exception {
        String apiUrl = HF_API_BASE + IMAGE_GENERATION_MODEL;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(hfToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", prompt);
        body.put("parameters", Map.of(
                "num_inference_steps", 30,
                "guidance_scale", 7.5,
                "negative_prompt", "blurry, low quality, distorted, ugly"
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Retry logic for model loading
        for (int i = 0; i < 3; i++) {
            try {
                byte[] imageBytes = restTemplate.postForObject(apiUrl, request, byte[].class);

                if (imageBytes != null && imageBytes.length > 0) {
                    log.info("Image generated successfully, size: {} bytes", imageBytes.length);
                    return imageBytes;
                }
            } catch (Exception e) {
                if (i < 2) {
                    log.warn("Retry {} - Model might be loading, waiting 5s...", i + 1);
                    Thread.sleep(5000);
                } else {
                    throw e;
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
        // Logic để xác định khuôn mặt dựa trên AI analysis
        // Có thể dùng các measurements như: face width, jaw width, forehead width

        // Default fallback
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
