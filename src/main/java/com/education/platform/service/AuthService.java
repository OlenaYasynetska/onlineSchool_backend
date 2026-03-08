package com.education.platform.service;

import com.education.platform.dto.request.LoginRequest;
import com.education.platform.dto.request.RegisterRequest;
import com.education.platform.dto.response.AuthResponse;
import com.education.platform.exception.ResourceNotFoundException;
import com.education.platform.model.Role;
import com.education.platform.model.User;
import com.education.platform.repository.UserRepository;
import com.education.platform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadCredentialsException("Email already registered");
        }
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(request.getRoles() != null ? request.getRoles() : Set.of(Role.STUDENT))
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        if (!user.isEnabled()) {
            throw new BadCredentialsException("Account is disabled");
        }
        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        String userId = jwtUtil.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (!user.isEnabled()) {
            throw new BadCredentialsException("Account is disabled");
        }
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String access = jwtUtil.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRoles().stream().toList()
        );
        String refresh = jwtUtil.generateRefreshToken(user.getId());
        Set<String> roles = user.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .userId(user.getId())
                .email(user.getEmail())
                .roles(roles)
                .expiresIn(expirationMs / 1000)
                .build();
    }
}
