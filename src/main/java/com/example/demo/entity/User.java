package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    @Column(name = "password", length = 255)
    private String password;
    private String fullName;
    private String phone;
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
}
