package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "hair_style_templates")
@Data
public class HairStyleTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Tên kiểu tóc
    private String description;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    private String gender; // MALE, FEMALE, UNISEX
    private String length; // SHORT, MEDIUM, LONG
    private String difficulty; // EASY, MEDIUM, HARD

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private Boolean isActive;
}