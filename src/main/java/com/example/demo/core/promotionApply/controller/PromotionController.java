package com.example.demo.core.promotionApply.controller;

import com.example.demo.core.promotionApply.modal.request.PromotionApplyRequest;
import com.example.demo.core.promotionApply.modal.response.PromotionApplyResponse;
import com.example.demo.core.promotionApply.modal.response.PromotionResponse;
import com.example.demo.core.promotionApply.modal.response.PromotionUsageResponse;
import com.example.demo.core.promotionApply.service.PromotionService;
import com.example.demo.uitl.ResponseObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-v1/promotions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PromotionController {

    private final PromotionService promotionService;

    /**
     * Helper method để lấy userId
     */
    private Long getUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }

    /**
     * Lấy tất cả promotion đang active (public)
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActivePromotions() {
        log.info("Getting all active promotions");
        List<PromotionResponse> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(new ResponseObject<>(promotions, "Lấy danh sách khuyến mãi thành công"));
    }

    /**
     * Lấy promotion available cho user hiện tại (chưa dùng)
     */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailablePromotions(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        log.info("Getting available promotions for user: {}", userId);
        List<PromotionResponse> promotions = promotionService.getAvailablePromotionsForUser(userId);
        return ResponseEntity.ok(new ResponseObject<>(promotions, "Lấy danh sách khuyến mãi khả dụng thành công"));
    }

    /**
     * Lấy tất cả promotion
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllPromotions() {
        log.info("Getting all promotions");
        List<PromotionResponse> promotions = promotionService.getAllPromotions();
        return ResponseEntity.ok(new ResponseObject<>(promotions, "Lấy danh sách khuyến mãi thành công"));
    }

    /**
     * Lấy danh sách promotion đã sử dụng
     */
    @GetMapping("/used")
    public ResponseEntity<?> getUsedPromotions(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        log.info("Getting used promotions for user: {}", userId);
        List<PromotionUsageResponse> usedPromotions = promotionService.getUsedPromotionsByUser(userId);
        return ResponseEntity.ok(new ResponseObject<>(usedPromotions, "Lấy lịch sử sử dụng khuyến mãi thành công"));
    }

    /**
     * Áp dụng promotion
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyPromotion(
            @RequestBody PromotionApplyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        log.info("User {} applying promotion: {}", userId, request.getCode());
        PromotionApplyResponse result = promotionService.applyPromotion(userId, request);
        return ResponseEntity.ok(new ResponseObject<>(result, "Áp dụng mã giảm giá thành công"));
    }

    /**
     * Kiểm tra user đã áp dụng promotion chưa (theo ID)
     * GET /api-v1/promotions/check-used/{promotionId}
     */
    @GetMapping("/check-used/{promotionId}")
    public ResponseEntity<?> checkPromotionUsedById(
            @PathVariable Long promotionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        log.info("Checking if user {} has used promotion {}", userId, promotionId);

        boolean isUsed = promotionService.hasUserUsedPromotion(userId, promotionId);

        return ResponseEntity.ok(new ResponseObject<>(
                Map.of(
                        "promotionId", promotionId,
                        "isUsed", isUsed,
                        "userId", userId
                ),
                isUsed ? "Bạn đã sử dụng mã này" : "Mã chưa được sử dụng"
        ));
    }

    /**
     * Kiểm tra user đã áp dụng promotion chưa (theo code)
     * GET /api-v1/promotions/check-used-by-code?code=GIAM20
     */
    @GetMapping("/check-used-by-code")
    public ResponseEntity<?> checkPromotionUsedByCode(
            @RequestParam String code,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        log.info("Checking if user {} has used promotion code: {}", userId, code);

        boolean isUsed = promotionService.hasUserUsedPromotionByCode(userId, code);

        return ResponseEntity.ok(new ResponseObject<>(
                Map.of(
                        "code", code,
                        "isUsed", isUsed,
                        "userId", userId
                ),
                isUsed ? "Bạn đã sử dụng mã này" : "Mã chưa được sử dụng"
        ));
    }

    /**
     * Kiểm tra nhiều promotion cùng lúc
     * POST /api-v1/promotions/check-used-batch
     * Body: { "promotionIds": [1, 2, 3] }
     */
    @PostMapping("/check-used-batch")
    public ResponseEntity<?> checkPromotionsUsedBatch(
            @RequestBody Map<String, List<Long>> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        List<Long> promotionIds = request.get("promotionIds");

        log.info("Checking batch promotions for user {}: {}", userId, promotionIds);

        Map<Long, Boolean> results = promotionService.checkPromotionsUsedBatch(userId, promotionIds);

        return ResponseEntity.ok(new ResponseObject<>(
                results,
                "Kiểm tra thành công"
        ));
    }
}