package com.example.demo.core.promotionApply.modal.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionApplyResponse {
    private String promotionCode;
    private Double discountPercent;
    private Double orderAmount;
    private Double discountAmount;
    private Double finalAmount;
    private String message;
}