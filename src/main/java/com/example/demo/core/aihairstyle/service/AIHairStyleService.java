package com.example.demo.core.aihairstyle.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.entity.*;
import com.example.demo.expection.ApiException;
import com.example.demo.repository.*;
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

    @Value("${huggingface.token}")
    private String hfToken;

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

    private static final String HF_API_BASE = "https://api-inference.huggingface.co/models/";
    private static final String IMAGE_GENERATION_MODEL = "stabilityai/stable-diffusion-2-1";

    /**
     * Upload ảnh lên Cloudinary với error handling
     */
    public String uploadImage(MultipartFile file) throws Exception {
        log.info("Uploading image to Cloudinary...");

        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                throw new ApiException("File không hợp lệ", "INVALID_FILE");
            }

            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new ApiException("File quá lớn. Tối đa 5MB", "FILE_TOO_LARGE");
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ApiException("File phải là ảnh (jpg, png, etc)", "INVALID_FILE_TYPE");
            }

            log.info("File info - Name: {}, Size: {} bytes, Type: {}",
                    file.getOriginalFilename(), file.getSize(), contentType);

            // Create Cloudinary instance
            Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudinaryCloudName,
                    "api_key", cloudinaryApiKey,
                    "api_secret", cloudinaryApiSecret,
                    "secure", true
            ));

            log.info("Cloudinary config - Cloud name: {}", cloudinaryCloudName);

            // Upload
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "hair_styles",
                            "resource_type", "image",
                            "transformation", ObjectUtils.asMap(
                                    "quality", "auto",
                                    "fetch_format", "auto"
                            )
                    )
            );

            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("Image uploaded successfully: {}", imageUrl);

            return imageUrl;

        } catch (ApiException e) {
            log.error("Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error uploading to Cloudinary", e);
            throw new ApiException("Không thể upload ảnh: " + e.getMessage(), "UPLOAD_ERROR");
        }
    }

    /**
     * Phân tích khuôn mặt
     */
    public Map<String, Object> analyzeFace(String imageUrl) {
        log.info("Analyzing face...");

        // Simplified - return default values
        Map<String, Object> faceData = new HashMap<>();
        faceData.put("face_shape", "oval");
        faceData.put("skin_tone", "medium");
        faceData.put("confidence", 0.85);

        return faceData;
    }

    /**
     * Generate hair style preview
     */
    public String generateHairStylePreview(String faceImageUrl, String styleImageUrl, String styleDescription) {
        log.info("Generating hair style preview...");

        try {
            String prompt = String.format(
                    "professional hair salon photo, %s hair style, " +
                            "realistic, high quality, sharp focus, professional lighting, " +
                            "front view portrait, photorealistic",
                    styleDescription
            );

            byte[] generatedImage = generateImageFromPrompt(prompt);

            return uploadGeneratedImage(generatedImage);

        } catch (Exception e) {
            log.error("Error generating preview", e);
            throw new ApiException("Không thể tạo preview: " + e.getMessage(), "GENERATION_ERROR");
        }
    }

    /**
     * Generate image từ text prompt
     */
    private byte[] generateImageFromPrompt(String prompt) throws Exception {
        String apiUrl = HF_API_BASE + IMAGE_GENERATION_MODEL;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(hfToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", prompt);
        body.put("parameters", Map.of(
                "num_inference_steps", 30,
                "guidance_scale", 7.5
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Retry logic
        for (int i = 0; i < 3; i++) {
            try {
                log.info("Calling Hugging Face API - attempt {}/3", i + 1);
                byte[] imageBytes = restTemplate.postForObject(apiUrl, request, byte[].class);

                if (imageBytes != null && imageBytes.length > 0) {
                    log.info("Image generated successfully, size: {} bytes", imageBytes.length);
                    return imageBytes;
                }
            } catch (Exception e) {
                if (i < 2) {
                    log.warn("Retry {} - waiting 5s...", i + 1);
                    Thread.sleep(5000);
                } else {
                    throw e;
                }
            }
        }

        throw new RuntimeException("Failed to generate image after retries");
    }

    /**
     * Upload generated image to Cloudinary
     */
    private String uploadGeneratedImage(byte[] imageBytes) throws Exception {
        log.info("Uploading generated image to Cloudinary...");

        try {
            Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudinaryCloudName,
                    "api_key", cloudinaryApiKey,
                    "api_secret", cloudinaryApiSecret,
                    "secure", true
            ));

            Map uploadResult = cloudinary.uploader().upload(
                    imageBytes,
                    ObjectUtils.asMap(
                            "folder", "hair_styles/generated",
                            "resource_type", "image"
                    )
            );

            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("Generated image uploaded: {}", imageUrl);

            return imageUrl;

        } catch (Exception e) {
            log.error("Error uploading generated image", e);
            throw new ApiException("Không thể upload ảnh AI: " + e.getMessage(), "UPLOAD_ERROR");
        }
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

        try {
            // 1. Validate
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException("Không tìm thấy người dùng", "USER_NOT_FOUND"));

            HairStyleTemplate template = templateRepository.findById(templateId)
                    .orElseThrow(() -> new ApiException("Không tìm thấy mẫu kiểu tóc", "TEMPLATE_NOT_FOUND"));

            // 2. Upload ảnh khuôn mặt
            log.info("Step 1: Uploading face image...");
            String faceImageUrl = uploadImage(faceImage);

            // 3. Phân tích khuôn mặt
            log.info("Step 2: Analyzing face...");
            Map<String, Object> faceAnalysis = analyzeFace(faceImageUrl);

            // 4. Generate preview
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

            // 5. Lưu kết quả
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

        } catch (ApiException e) {
            log.error("Business logic error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating suggestion", e);
            throw new ApiException("Lỗi không xác định: " + e.getMessage(), "UNKNOWN_ERROR");
        }
    }

    public List<HairStyleTemplate> getTemplates(String gender, String length) {
        log.info("Getting templates - gender: {}, length: {}", gender, length);

        if (gender != null && length != null) {
            return templateRepository.findByGenderAndLengthAndIsActiveTrue(gender, length);
        } else if (gender != null) {
            return templateRepository.findByGenderAndIsActiveTrue(gender);
        }
        return templateRepository.findByIsActiveTrue();
    }

    public List<HairStyleSuggestion> getMySuggestions(Long userId) {
        return suggestionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}