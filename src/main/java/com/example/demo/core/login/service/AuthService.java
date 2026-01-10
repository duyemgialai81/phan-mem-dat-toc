package com.example.demo.core.login.service;


import com.example.demo.core.login.modal.request.LoginRequest;
import com.example.demo.core.login.modal.request.RegisterRequest;
import com.example.demo.core.login.modal.response.LoginResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.UserSession;
import com.example.demo.expection.ApiException;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserSessionRepository;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.uitl.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserSessionRepository sessionRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest loginRequest, HttpServletRequest request) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ApiException("Tài khoản không tồn tại", "USER_NOT_FOUND"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new ApiException("Mật khẩu không chính xác", "INVALID_PASSWORD");
        }
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        saveUserSession(user, accessToken, refreshToken, request);
        String roleName = (user.getRole() != null) ? user.getRole().getName() : "USER";

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                user.getFullName(),
                user.getUsername(),
                user.getPhone(),
                roleName
        );
    }
    public User register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ApiException("Tên đăng nhập đã tồn tại", "USERNAME_ALREADY_EXISTS");
        }
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        newUser.setPassword(encodedPassword);
        newUser.setFullName(registerRequest.getFullName());
        newUser.setPhone(registerRequest.getPhone());
        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new ApiException("Role not found", "404"));
        newUser.setRole(defaultRole);
        return userRepository.save(newUser);
    }

    private void saveUserSession(User user, String accessToken, String refreshToken, HttpServletRequest request) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setTokenType("Bearer");
        String ip = RequestUtils.getClientIp(request);
        session.setIpAddress(ip);
        session.setDeviceType(RequestUtils.getDeviceType(request));
        session.setLoginLocation(LocationService.getLocation(ip));
        session.setLoginTime(LocalDateTime.now());
        session.setLastActive(LocalDateTime.now());
        session.setRevoked(false);

        sessionRepository.save(session);
    }
}