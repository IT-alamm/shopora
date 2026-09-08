package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.AuthDtos.*;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.exception.BadRequestException;
import com.ecommerce.auth.exception.UnauthorizedException;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthResponse register(RegisterRequest req) {
        if (users.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .role("ROLE_USER")
                .build();
        user = users.save(user);
        String token = jwt.generateToken(user.getId(), user.getEmail(), user.getRole());
        return AuthResponse.builder()
                .token(token).tokenType("Bearer").role(user.getRole())
                .userId(user.getId()).email(user.getEmail()).build();
    }

    public AuthResponse login(LoginRequest req) {
        User user = users.findByEmail(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        String token = jwt.generateToken(user.getId(), user.getEmail(), user.getRole());
        return AuthResponse.builder()
                .token(token).tokenType("Bearer").role(user.getRole())
                .userId(user.getId()).email(user.getEmail()).build();
    }
}
