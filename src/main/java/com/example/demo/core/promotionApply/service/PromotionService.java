package com.example.demo.core.promotionApply.service;

import com.example.demo.core.promotionApply.modal.request.PromotionApplyRequest;
import com.example.demo.core.promotionApply.modal.response.PromotionApplyResponse;
import com.example.demo.core.promotionApply.modal.response.PromotionResponse;
import com.example.demo.entity.Promotion;
import com.example.demo.expection.ApiException;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.uitl.MapperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
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

    public PromotionApplyResponse applyPromotion(PromotionApplyRequest request) {
        log.info("Applying promotion with code: {}, orderAmount: {}",
                request.getCode(), request.getOrderAmount());

        try {
            // Tìm promotion
            Promotion promotion = promotionRepository.findByCode(request.getCode())
                    .orElseThrow(() -> new ApiException("Mã khuyến mãi không hợp lệ", "INVALID_CODE"));

            log.info("Found promotion: {}, discount: {}%",
                    promotion.getCode(), promotion.getDiscountPercent());

            // Validate expiry date
            if (promotion.getExpiryDate().isBefore(LocalDate.now())) {
                throw new ApiException("Mã khuyến mãi đã hết hạn", "EXPIRED_CODE");
            }

            // Tính toán
            Double orderAmount = request.getOrderAmount() != null ? request.getOrderAmount() : 0.0;
            Double discountAmount = orderAmount * (promotion.getDiscountPercent() / 100.0);
            Double finalAmount = orderAmount - discountAmount;

            // Tạo response object
            PromotionApplyResponse response = new PromotionApplyResponse();
            response.setPromotionCode(promotion.getCode());
            response.setDiscountPercent(promotion.getDiscountPercent());
            response.setOrderAmount(orderAmount);
            response.setDiscountAmount(discountAmount);
            response.setFinalAmount(finalAmount);
            response.setMessage("Áp dụng mã giảm giá thành công!");

            log.info("Applied promotion successfully. Final amount: {}", finalAmount);
            return response;

        } catch (ApiException e) {
            log.error("ApiException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error applying promotion", e);
            throw new RuntimeException("Lỗi khi áp dụng mã khuyến mãi: " + e.getMessage());
        }
    }

    private PromotionResponse mapToResponse(Promotion promotion) {
        // Sử dụng MapperUtils để map entity sang response
        PromotionResponse response = MapperUtils.map(promotion, PromotionResponse.class);

        // Set custom field isActive (không có trong entity)
        response.setIsActive(promotion.getExpiryDate() != null &&
                (promotion.getExpiryDate().isAfter(LocalDate.now()) ||
                        promotion.getExpiryDate().isEqual(LocalDate.now())));

        return response;
    }
}