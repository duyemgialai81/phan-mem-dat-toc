package com.example.demo.core.login.modal.response;

import com.example.demo.entity.User;
import com.example.demo.entity.UserSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String fullName;
    private String username;
    private String phone;
    private String nameRole;
    public LoginResponse(UserSession userSession, User user) {
        this.accessToken =userSession.getAccessToken();
        this.refreshToken =userSession.getRefreshToken();
        this.tokenType =userSession.getTokenType();
        this.fullName = user.getFullName(); // Lấy từ thực thể User
        this.username = user.getUsername();
        this.phone = user.getPhone();
        this.nameRole= user.getRole().getName();
    }
}
