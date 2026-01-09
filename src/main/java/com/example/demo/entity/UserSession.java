package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Data
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String deviceToken;     // Dùng cho thông báo Push (nếu có)
    private String deviceType;      // Ví dụ: Chrome on Windows, Postman
    private String ipAddress;       // Địa chỉ IP người dùng
    private String loginLocation;   // Vị trí (ví dụ: Gia Lai, Vietnam)
    private LocalDateTime loginTime;
    private LocalDateTime lastActive;
    private boolean isRevoked;      // Trạng thái phiên (true nếu đã bị hủy)

    @Column(columnDefinition = "TEXT") // Token rất dài nên để TEXT
    private String accessToken;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    private String tokenType;

    public void setRevoked(boolean revoked) {
        isRevoked = revoked;
    }
}
