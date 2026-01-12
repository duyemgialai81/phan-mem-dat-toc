package com.example.demo.core.promotionApply.controller;

import com.example.demo.core.promotionApply.modal.request.PromotionApplyRequest;
import com.example.demo.core.promotionApply.modal.response.PromotionApplyResponse;
import com.example.demo.core.promotionApply.modal.response.PromotionResponse;
import com.example.demo.core.promotionApply.service.PromotionService;
import com.example.demo.uitl.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping("/active")
    public ResponseEntity<?> getActivePromotions() {
        List<PromotionResponse> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(new ResponseObject<>(promotions, "Lấy danh sách khuyến mãi thành công"));
    }
    @GetMapping("/all")
    public ResponseEntity<?> getAllPromotions() {
        List<PromotionResponse> promotions = promotionService.getAllPromotions();
        return ResponseEntity.ok(new ResponseObject<>(promotions, "Lấy danh sách khuyến mãi thành công"));
    }
    @PostMapping("/apply")
    public ResponseEntity<?> applyPromotion(@RequestBody PromotionApplyRequest request) {
        PromotionApplyResponse result = promotionService.applyPromotion(request);
        return ResponseEntity.ok(new ResponseObject<>(result, "Áp dụng mã giảm giá thành công"));
    }
}
