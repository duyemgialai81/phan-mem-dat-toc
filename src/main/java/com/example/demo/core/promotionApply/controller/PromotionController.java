package com.example.demo.core.promotionApply.controller;

import com.example.demo.core.promotionApply.modal.request.PromotionApplyRequest;
import com.example.demo.core.promotionApply.modal.response.PromotionApplyResponse;
import com.example.demo.core.promotionApply.modal.response.PromotionResponse;
import com.example.demo.core.promotionApply.service.PromotionService;
import com.example.demo.uitl.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api-v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    private Long getUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }

    // Lấy tất cả promotion đang active (public)
    @GetMapping("/active")
    public ResponseEntity<?> getActivePromotions() {
        List<PromotionResponse> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(new ResponseObject<>(promotions, "Lấy danh sách khuyến mãi thành công"));
    }

    // Lấy promotion available cho user hiện tại (chưa dùng)
    @GetMapping("/available")
    public ResponseEntity<?> getAvailablePromotions(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        List<PromotionResponse> promotions = promotionService.getAvailablePromotionsForUser(userId);
        return ResponseEntity.ok(new ResponseObject<>(promotions, "Lấy danh sách khuyến mãi khả dụng thành công"));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllPromotions() {
        List<PromotionResponse> promotions = promotionService.getAllPromotions();
        return ResponseEntity.ok(new ResponseObject<>(promotions, "Lấy danh sách khuyến mãi thành công"));
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyPromotion(
            @RequestBody PromotionApplyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        PromotionApplyResponse result = promotionService.applyPromotion(userId, request);
        return ResponseEntity.ok(new ResponseObject<>(result, "Áp dụng mã giảm giá thành công"));
    }
}