package com.example.demo.core.review.controller;

import com.example.demo.core.review.modal.request.ReviewRequest;
import com.example.demo.core.review.modal.response.ReviewResponse;
import com.example.demo.core.review.service.ReviewService;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.uitl.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api-v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtTokenProvider jwtTokenProvider;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String userId = jwtTokenProvider.getUserIdFromToken(token);
            return Long.parseLong(userId);
        }
        throw new RuntimeException("Token không hợp lệ");
    }

    @PostMapping
    public ResponseEntity<?> createReview(
             @RequestBody ReviewRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        ReviewResponse response = reviewService.createReview(userId, request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Tạo đánh giá thành công"));
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<?> getMyReviews(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<ReviewResponse> reviews = reviewService.getMyReviews(userId);
        return ResponseEntity.ok(new ResponseObject<>(reviews, "Lấy danh sách đánh giá thành công"));
    }
}
