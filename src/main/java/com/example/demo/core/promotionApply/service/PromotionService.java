package com.example.demo.core.promotionApply.service;

import com.example.demo.core.promotionApply.modal.request.PromotionApplyRequest;
import com.example.demo.core.promotionApply.modal.response.PromotionResponse;
import com.example.demo.entity.Promotion;
import com.example.demo.expection.ApiException;
import com.example.demo.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public List<PromotionResponse> getActivePromotions() {
        return promotionRepository.findByExpiryDateGreaterThanEqual(LocalDate.now())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Map<String, Object> applyPromotion(PromotionApplyRequest request) {
        log.info("Applying promotion with code: {}, orderAmount: {}",
                request.getCode(), request.getOrderAmount());

        try {
            Promotion promotion = promotionRepository.findByCode(request.getCode())
                    .orElseThrow(() -> new ApiException("Mã khuyến mãi không hợp lệ", "INVALID_CODE"));

            log.info("Found promotion: {}, discount: {}%",
                    promotion.getCode(), promotion.getDiscountPercent());

            if (promotion.getExpiryDate().isBefore(LocalDate.now())) {
                throw new ApiException("Mã khuyến mãi đã hết hạn", "EXPIRED_CODE");
            }

            Double orderAmount = request.getOrderAmount() != null ? request.getOrderAmount() : 0.0;
            Double discountAmount = orderAmount * (promotion.getDiscountPercent() / 100.0);
            Double finalAmount = orderAmount - discountAmount;

            Map<String, Object> result = new HashMap<>();
            result.put("promotionCode", promotion.getCode());
            result.put("discountPercent", promotion.getDiscountPercent());
            result.put("orderAmount", orderAmount);
            result.put("discountAmount", discountAmount);
            result.put("finalAmount", finalAmount);
            result.put("message", "Áp dụng mã giảm giá thành công!");

            log.info("Applied promotion successfully. Final amount: {}", finalAmount);
            return result;

        } catch (ApiException e) {
            log.error("ApiException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error applying promotion", e);
            throw new RuntimeException("Lỗi khi áp dụng mã khuyến mãi: " + e.getMessage());
        }
    }

    private PromotionResponse mapToResponse(Promotion promotion) {
        PromotionResponse response = new PromotionResponse();
        response.setId(promotion.getId());
        response.setCode(promotion.getCode());
        response.setDiscountPercent(promotion.getDiscountPercent());
        response.setExpiryDate(promotion.getExpiryDate());
        response.setIsActive(promotion.getExpiryDate().isAfter(LocalDate.now()) ||
                promotion.getExpiryDate().isEqual(LocalDate.now()));
        return response;
    }
}
