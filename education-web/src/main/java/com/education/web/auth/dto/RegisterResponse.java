package com.education.web.auth.dto;

/** Відповідь після реєстрації — без JWT, поки email не підтверджено. */
public record RegisterResponse(
        String message,
        String email,
        boolean emailVerificationRequired
) {
}
