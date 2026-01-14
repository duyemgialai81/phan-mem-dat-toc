package com.example.demo.core.aihairstyle.controller;

import com.example.demo.core.aihairstyle.service.AIHairChatService;
import com.example.demo.core.aihairstyle.service.AIHairStyleService;
import com.example.demo.entity.HairStyleSuggestion;
import com.example.demo.entity.HairStyleTemplate;
import com.example.demo.uitl.ResponseObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-v1/ai-hairstyle")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AIHairStyleController {

    private final AIHairStyleService aiHairStyleService;
    private final AIHairChatService aiHairChatService;

    private Long getUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }

    @GetMapping("/templates")
    public ResponseEntity<?> getTemplates(
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String length
    ) {
        try {
            log.info("Getting templates - gender: {}, length: {}", gender, length);
            List<HairStyleTemplate> templates = aiHairStyleService.getTemplates(gender, length);
            return ResponseEntity.ok(new ResponseObject<>(templates, "Lấy danh sách mẫu thành công"));
        } catch (Exception e) {
            log.error("Error getting templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(null, "Lỗi: " + e.getMessage()));
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generatePreview(
            @RequestParam("faceImage") MultipartFile faceImage,
            @RequestParam("templateId") Long templateId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            log.info("Generate preview request - templateId: {}", templateId);

            // Validate file
            if (faceImage.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseObject<>(null, "File ảnh không hợp lệ"));
            }

            // Validate file size (5MB)
            if (faceImage.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(new ResponseObject<>(null, "Kích thước file không được vượt quá 5MB"));
            }

            // Validate file type
            String contentType = faceImage.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(new ResponseObject<>(null, "File phải là ảnh (PNG, JPG, JPEG)"));
            }

            Long userId = getUserId(userDetails);
            log.info("Processing for user: {}", userId);

            HairStyleSuggestion suggestion = aiHairStyleService.createSuggestion(
                    userId, faceImage, templateId
            );

            return ResponseEntity.ok(new ResponseObject<>(suggestion, "Tạo preview thành công"));

        } catch (RuntimeException e) {
            log.error("Runtime error generating preview", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject<>(null, e.getMessage()));
        } catch (Exception e) {
            log.error("Error generating preview", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(null, "Lỗi server: " + e.getMessage()));
        }
    }

    @GetMapping("/my-suggestions")
    public ResponseEntity<?> getMySuggestions(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = getUserId(userDetails);
            log.info("Getting suggestions for user: {}", userId);
            List<HairStyleSuggestion> suggestions = aiHairStyleService.getMySuggestions(userId);
            return ResponseEntity.ok(new ResponseObject<>(suggestions, "Lấy lịch sử thành công"));
        } catch (Exception e) {
            log.error("Error getting suggestions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(null, "Lỗi: " + e.getMessage()));
        }
    }

    // ========== AI CHAT ENDPOINTS ==========

    /**
     * Chat với AI advisor về chăm sóc tóc
     */
    @PostMapping("/chat-advisor")
    public ResponseEntity<?> chatAdvisor(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            String message = (String) request.get("message");
            String hairType = (String) request.getOrDefault("hair_type", "normal");

            @SuppressWarnings("unchecked")
            List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseObject<>(null, "Tin nhắn không được để trống"));
            }

            log.info("Chat request from user: {}, message: {}", userDetails.getUsername(), message);

            Map<String, Object> response = aiHairChatService.chatWithAdvisor(message, hairType, history);

            return ResponseEntity.ok(new ResponseObject<>(response, "Chat thành công"));

        } catch (Exception e) {
            log.error("Error in chat advisor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(null, "Lỗi: " + e.getMessage()));
        }
    }

    /**
     * Phân tích tình trạng tóc từ ảnh
     */
    @PostMapping("/analyze-hair")
    public ResponseEntity<?> analyzeHair(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            String imageUrl = request.get("image_url");

            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseObject<>(null, "URL ảnh không được để trống"));
            }

            log.info("Analyze hair request from user: {}, image: {}", userDetails.getUsername(), imageUrl);

            Map<String, Object> analysis = aiHairChatService.analyzeHairFromImage(imageUrl);

            return ResponseEntity.ok(new ResponseObject<>(analysis, "Phân tích thành công"));

        } catch (Exception e) {
            log.error("Error analyzing hair", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(null, "Lỗi: " + e.getMessage()));
        }
    }
}