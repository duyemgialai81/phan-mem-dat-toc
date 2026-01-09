package com.example.demo.core.login.modal.response;

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
    public LoginResponse(UserSession userSession) {
        this.accessToken =userSession.getAccessToken();
        this.refreshToken =userSession.getRefreshToken();
        this.tokenType =userSession.getTokenType();
    }
}
