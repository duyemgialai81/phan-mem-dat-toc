package com.example.demo.core.promotionApply.service;

import com.example.demo.core.promotionApply.modal.request.PromotionApplyRequest;
import com.example.demo.core.promotionApply.modal.response.PromotionApplyResponse;
import com.example.demo.core.promotionApply.modal.response.PromotionResponse;
import com.example.demo.core.promotionApply.modal.response.PromotionUsageResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final UserPromotionRepository userPromotionRepository;
    private final UserRepository userRepository;

    /**
     * Lấy tất cả promotion đang active
     */
    public List<PromotionResponse> getActivePromotions() {
        return promotionRepository.findByExpiryDateGreaterThanEqual(LocalDate.now())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả promotion
     */
    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra user đã sử dụng promotion chưa (theo ID)
     */
    public boolean hasUserUsedPromotion(Long userId, Long promotionId) {
        log.info("Checking if user {} has used promotion {}", userId, promotionId);
        return userPromotionRepository.existsByUserIdAndPromotionId(userId, promotionId);
    }

    /**
     * Kiểm tra user đã sử dụng promotion chưa (theo code)
     */
    public boolean hasUserUsedPromotionByCode(Long userId, String code) {
        log.info("Checking if user {} has used promotion code: {}", userId, code);

        // Tìm promotion theo code
        Promotion promotion = promotionRepository.findByCode(code)
                .orElseThrow(() -> new ApiException("Mã giảm giá không tồn tại", "CODE_NOT_FOUND"));

        return userPromotionRepository.existsByUserIdAndPromotionId(userId, promotion.getId());
    }

    /**
     * Kiểm tra nhiều promotion cùng lúc
     */
    public Map<Long, Boolean> checkPromotionsUsedBatch(Long userId, List<Long> promotionIds) {
        log.info("Checking batch promotions for user {}: {}", userId, promotionIds);

        Map<Long, Boolean> results = new HashMap<>();

        for (Long promotionId : promotionIds) {
            boolean isUsed = userPromotionRepository.existsByUserIdAndPromotionId(userId, promotionId);
            results.put(promotionId, isUsed);
        }

        return results;
    }

    /**
     * Lấy danh sách promotion đã sử dụng
     */
    public List<PromotionUsageResponse> getUsedPromotionsByUser(Long userId) {
        log.info("Getting used promotions for user: {}", userId);

        List<UserPromotion> usages = userPromotionRepository.findByUserIdOrderByUsedAtDesc(userId);

        return usages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy promotion available cho user (chưa dùng)
     */
    public List<PromotionResponse> getAvailablePromotionsForUser(Long userId) {
        log.info("Getting available promotions for user: {}", userId);

        // Lấy tất cả promotion còn hạn
        List<Promotion> activePromotions = promotionRepository
                .findByExpiryDateGreaterThanEqual(LocalDate.now());

        // Filter ra những mã user chưa dùng và map sang response
        return activePromotions.stream()
                .map(promotion -> {
                    PromotionResponse response = mapToResponse(promotion);
                    // Đánh dấu promotion đã dùng hay chưa
                    response.setIsUsed(userPromotionRepository.existsByUserIdAndPromotionId(userId, promotion.getId()));
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Áp dụng promotion
     */
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
            if (hasUserUsedPromotion(userId, promotion.getId())) {
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

    /**
     * Map Promotion entity sang PromotionResponse
     */
    private PromotionResponse mapToResponse(Promotion promotion) {
        PromotionResponse response = MapperUtils.map(promotion, PromotionResponse.class);
        response.setIsActive(promotion.getExpiryDate() != null &&
                (promotion.getExpiryDate().isAfter(LocalDate.now()) ||
                        promotion.getExpiryDate().isEqual(LocalDate.now())));
        return response;
    }

    /**
     * Convert UserPromotion entity sang PromotionUsageResponse
     */
    private PromotionUsageResponse convertToDTO(UserPromotion userPromotion) {
        PromotionUsageResponse dto = new PromotionUsageResponse();
        dto.setId(userPromotion.getId());
        dto.setPromotionId(userPromotion.getPromotion().getId());
        dto.setPromotionCode(userPromotion.getPromotion().getCode());
        dto.setDiscountPercent(userPromotion.getPromotion().getDiscountPercent());
        dto.setUsedAt(userPromotion.getUsedAt());
        dto.setOrderAmount(userPromotion.getOrderAmount());
        dto.setDiscountAmount(userPromotion.getDiscountAmount());

        // Tính finalAmount
        double finalAmount = userPromotion.getOrderAmount() - userPromotion.getDiscountAmount();
        dto.setFinalAmount(finalAmount);

        return dto;
    }
}