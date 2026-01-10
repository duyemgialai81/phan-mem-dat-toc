package com.example.demo.core.login.controller;

import com.example.demo.core.login.service.AuthService;
import com.example.demo.core.login.modal.request.LoginRequest;
import com.example.demo.core.login.modal.request.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.uitl.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-v1")
@CrossOrigin("*")
public class LoginAndRegisterController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseObject<?> login (@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        return new ResponseObject<>( authService.login(loginRequest,request));
    }
    @PostMapping("/register")
    public ResponseObject<?> register(@RequestBody RegisterRequest registerRequest) {
        User user = authService.register(registerRequest);
        return new ResponseObject<>("Đăng ký thành công tài khoản: " + user.getUsername());
    }
}
