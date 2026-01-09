package com.example.demo.entity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "reviews")
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer star; // 1-5 sao
    private String comment;

    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;
}