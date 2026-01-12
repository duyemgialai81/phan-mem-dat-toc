package com.example.demo.core.review.controller;

import com.example.demo.core.review.modal.request.ReviewRequest;
import com.example.demo.core.review.modal.response.ReviewResponse;
import com.example.demo.core.review.service.ReviewService;
import com.example.demo.uitl.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api-v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    private Long getUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }

    @PostMapping
    public ResponseEntity<?> createReview(
            @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        ReviewResponse response = reviewService.createReview(userId, request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Tạo đánh giá thành công"));
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<?> getMyReviews(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        List<ReviewResponse> reviews = reviewService.getMyReviews(userId);
        return ResponseEntity.ok(new ResponseObject<>(reviews, "Lấy danh sách đánh giá thành công"));
    }
}