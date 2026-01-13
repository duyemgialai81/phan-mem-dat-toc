package com.example.demo.core.promotionApply.modal.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionUsageResponse {
    private Long id;
    private Long promotionId;
    private String promotionCode;
    private Double discountPercent;
    private LocalDateTime usedAt;
    private Double orderAmount;
    private Double discountAmount;
    private Double finalAmount;
}
