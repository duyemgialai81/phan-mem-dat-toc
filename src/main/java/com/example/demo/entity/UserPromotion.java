package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_promotions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "promotion_id"})
)
@Data
public class UserPromotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    private LocalDateTime usedAt;

    private Double orderAmount;
    private Double discountAmount;
}