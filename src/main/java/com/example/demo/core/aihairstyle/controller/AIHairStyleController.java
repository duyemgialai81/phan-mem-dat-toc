package com.example.demo.core.aihairstyle.controller;

import com.example.demo.core.aihairstyle.service.AIHairStyleService;
import com.example.demo.entity.HairStyleSuggestion;
import com.example.demo.entity.HairStyleTemplate;

import com.example.demo.uitl.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api-v1/ai-hairstyle")
@RequiredArgsConstructor
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
        List<HairStyleTemplate> templates = aiHairStyleService.getTemplates(gender, length);
        return ResponseEntity.ok(new ResponseObject<>(templates, "Lấy danh sách mẫu thành công"));
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generatePreview(
            @RequestParam("faceImage") MultipartFile faceImage,
            @RequestParam("templateId") Long templateId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            Long userId = getUserId(userDetails);
            HairStyleSuggestion suggestion = aiHairStyleService.createSuggestion(
                    userId, faceImage, templateId
            );
            return ResponseEntity.ok(new ResponseObject<>(suggestion, "Tạo preview thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseObject<>(null, "Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/my-suggestions")
    public ResponseEntity<?> getMySuggestions(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        // Implementation
        return ResponseEntity.ok(new ResponseObject<>(null, "Success"));
    }
}