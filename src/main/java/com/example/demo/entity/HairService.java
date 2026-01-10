package com.example.demo.entity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "services")
@Data
public class HairService { // Tránh trùng tên Service của Spring
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Double price;
    private Integer durationMin; // Thời gian làm (phút)
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
