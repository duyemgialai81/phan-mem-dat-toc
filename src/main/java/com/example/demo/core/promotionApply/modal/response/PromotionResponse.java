package com.example.demo.core.promotionApply.modal.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionResponse {
    private Long id;
    private String code;
    private Double discountPercent;
    private LocalDate expiryDate;
    private Boolean isActive;
    private Boolean IsUsed;

}