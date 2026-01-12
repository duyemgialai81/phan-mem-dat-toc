    package com.example.demo.entity;
    import jakarta.persistence.*;
    import lombok.Data;
    @Entity
    @Table(name = "barbers")
    @Data
    public class Barber {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String experience;
        private Double rating; // Điểm đánh giá trung bình

        @OneToOne
        @JoinColumn(name = "user_id")
        private User user; // Mỗi Barber là một User có Role BARBER
    }
