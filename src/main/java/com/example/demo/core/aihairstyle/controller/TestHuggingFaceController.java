package com.example.demo.core.aihairstyle.controller;

import com.example.demo.core.aihairstyle.service.AIHairStyleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api-v1/test")
public class TestHuggingFaceController {

    @Autowired
    private AIHairStyleService aiService;

    @GetMapping("/hf-test")
    public ResponseEntity<?> testHuggingFace() {
        try {
            byte[] image = aiService.generateImageFromPrompt(
                    "professional short hair style, realistic, high quality"
            );

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "imageSize", image.length,
                    "message", "Hugging Face is working!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}