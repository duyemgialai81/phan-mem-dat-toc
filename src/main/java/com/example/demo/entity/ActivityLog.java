package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Data
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String action; // Ví dụ: "LOGIN", "BOOK_APPOINTMENT", "LOGOUT"
    private String description; // Ví dụ: "Đã đặt lịch cắt tóc tại chi nhánh A"
    private String ipAddress;
    private LocalDateTime timestamp;
}