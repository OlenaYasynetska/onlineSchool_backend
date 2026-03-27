package com.education.web.auth.dto;

public record AuthResponse(
        AuthUserResponse user,
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}

