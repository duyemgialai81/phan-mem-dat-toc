package com.example.demo.core.promotionApply.modal.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionApplyRequest {
    private String code;
    private Double orderAmount;
}
