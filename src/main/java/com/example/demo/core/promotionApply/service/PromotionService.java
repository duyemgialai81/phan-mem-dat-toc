package com.example.demo.core.promotionApply.service;

import com.example.demo.core.promotionApply.modal.request.PromotionApplyRequest;
import com.example.demo.core.promotionApply.modal.response.PromotionApplyResponse;
import com.example.demo.core.promotionApply.modal.response.PromotionResponse;
import com.example.demo.entity.Promotion;
import com.example.demo.entity.User;
import com.example.demo.entity.UserPromotion;
import com.example.demo.expection.ApiException;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.UserPromotionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.uitl.MapperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final UserPromotionRepository userPromotionRepository;
    private final UserRepository userRepository;

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

    @Transactional
    public PromotionApplyResponse applyPromotion(Long userId, PromotionApplyRequest request) {
        log.info("User {} applying promotion with code: {}, orderAmount: {}",
                userId, request.getCode(), request.getOrderAmount());

        try {
            // Tìm user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException("Không tìm thấy người dùng", "USER_NOT_FOUND"));

            // Tìm promotion
            Promotion promotion = promotionRepository.findByCode(request.getCode())
                    .orElseThrow(() -> new ApiException("Mã khuyến mãi không hợp lệ", "INVALID_CODE"));

            log.info("Found promotion: {}, discount: {}%",
                    promotion.getCode(), promotion.getDiscountPercent());

            // Kiểm tra user đã dùng mã này chưa
            if (userPromotionRepository.existsByUserIdAndPromotionId(userId, promotion.getId())) {
                throw new ApiException("Bạn đã sử dụng mã khuyến mãi này rồi", "ALREADY_USED");
            }

            // Validate expiry date
            if (promotion.getExpiryDate().isBefore(LocalDate.now())) {
                throw new ApiException("Mã khuyến mãi đã hết hạn", "EXPIRED_CODE");
            }

            // Tính toán
            Double orderAmount = request.getOrderAmount() != null ? request.getOrderAmount() : 0.0;
            Double discountAmount = orderAmount * (promotion.getDiscountPercent() / 100.0);
            Double finalAmount = orderAmount - discountAmount;

            // Lưu lại việc user đã dùng mã này
            UserPromotion userPromotion = new UserPromotion();
            userPromotion.setUser(user);
            userPromotion.setPromotion(promotion);
            userPromotion.setUsedAt(LocalDateTime.now());
            userPromotion.setOrderAmount(orderAmount);
            userPromotion.setDiscountAmount(discountAmount);
            userPromotionRepository.save(userPromotion);

            // Tạo response object
            PromotionApplyResponse response = new PromotionApplyResponse();
            response.setPromotionCode(promotion.getCode());
            response.setDiscountPercent(promotion.getDiscountPercent());
            response.setOrderAmount(orderAmount);
            response.setDiscountAmount(discountAmount);
            response.setFinalAmount(finalAmount);
            response.setMessage("Áp dụng mã giảm giá thành công!");

            log.info("User {} applied promotion successfully. Final amount: {}", userId, finalAmount);
            return response;

        } catch (ApiException e) {
            log.error("ApiException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error applying promotion", e);
            throw new RuntimeException("Lỗi khi áp dụng mã khuyến mãi: " + e.getMessage());
        }
    }

    public List<PromotionResponse> getAvailablePromotionsForUser(Long userId) {
        // Lấy tất cả promotion còn hạn
        List<Promotion> activePromotions = promotionRepository
                .findByExpiryDateGreaterThanEqual(LocalDate.now());

        // Filter ra những mã user chưa dùng
        return activePromotions.stream()
                .filter(promotion -> !userPromotionRepository.existsByUserIdAndPromotionId(userId, promotion.getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PromotionResponse mapToResponse(Promotion promotion) {
        PromotionResponse response = MapperUtils.map(promotion, PromotionResponse.class);
        response.setIsActive(promotion.getExpiryDate() != null &&
                (promotion.getExpiryDate().isAfter(LocalDate.now()) ||
                        promotion.getExpiryDate().isEqual(LocalDate.now())));
        return response;
    }
}