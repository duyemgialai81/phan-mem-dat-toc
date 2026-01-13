package com.example.demo.core.aihairstyle.controller;

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

@RestController
@RequestMapping("/api-v1/ai-hairstyle")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AIHairStyleController {

    private final AIHairStyleService aiHairStyleService;

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
}