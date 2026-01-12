package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "hair_style_suggestions")
@Data
public class HairStyleSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String originalImageUrl;

    @Column(columnDefinition = "TEXT")
    private String styleImageUrl;

    @Column(columnDefinition = "TEXT")
    private String resultImageUrl;

    private String faceShape;
    private String skinTone;
    private String hairTexture;
    private String hairLength;

    @Column(columnDefinition = "TEXT")
    private String aiAnalysis;

    private Integer confidenceScore; // Độ tin cậy 0-100

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "recommended_service_id")
    private HairService recommendedService;
}