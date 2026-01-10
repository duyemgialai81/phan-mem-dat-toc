package com.example.demo.core.promotionApply.modal.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PromotionResponse {
    private Long id;
    private String code;
    private Double discountPercent;
    private LocalDate expiryDate;
    private Boolean isActive;
}