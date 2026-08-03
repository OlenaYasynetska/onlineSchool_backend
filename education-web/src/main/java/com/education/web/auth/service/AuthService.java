package com.education.web.auth.service;

import com.education.web.auth.dto.AuthResponse;
import com.education.web.auth.dto.AuthUserResponse;
import com.education.web.auth.dto.ForgotPasswordRequest;
import com.education.web.auth.dto.LoginRequest;
import com.education.web.auth.dto.MessageResponse;
import com.education.web.auth.dto.RegisterRequest;
import com.education.web.auth.dto.RegisterResponse;
import com.education.web.auth.dto.ResendVerificationRequest;
import com.education.web.auth.dto.ResetPasswordRequest;
import com.education.web.auth.dto.VerifyEmailResponse;
import com.education.web.auth.model.OrganizationEntity;
import com.education.web.auth.model.SubscriptionPlanEntity;
import com.education.web.auth.model.UserEntity;
import com.education.web.auth.model.UserRole;
import com.education.web.auth.repository.OrganizationJpaRepository;
import com.education.web.auth.repository.SubscriptionPlanJpaRepository;
import com.education.web.auth.repository.UserJpaRepository;
import com.education.web.mail.AuthMailService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {
    private static final long EMAIL_VERIFICATION_TTL_HOURS = 24;
    private static final long PASSWORD_RESET_TTL_HOURS = 1;

    private final UserJpaRepository users;
    private final OrganizationJpaRepository organizations;
    private final SubscriptionPlanJpaRepository plans;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMailService authMailService;

    public AuthService(
            UserJpaRepository users,
            OrganizationJpaRepository organizations,
            SubscriptionPlanJpaRepository plans,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthMailService authMailService
    ) {
        this.users = users;
        this.organizations = organizations;
        this.plans = plans;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authMailService = authMailService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) {
            throw new BadCredentialsException("Email already registered");
        }

        UserEntity user = new UserEntity();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setRole(UserRole.ADMIN_SCHOOL);
        user.setEnabled(true);
        user.setEmailVerified(false);
        issueEmailVerificationToken(user);
        user = users.save(user);

        String planKey = request.plan().trim().toLowerCase();
        SubscriptionPlanEntity plan = plans.findByPlanKeyIgnoreCase(planKey)
                .orElseGet(() -> {
                    SubscriptionPlanEntity created = new SubscriptionPlanEntity();
                    created.setPlanKey(planKey);
                    created.setTitle(switch (planKey) {
                        case "pro" -> "Pro Plan";
                        case "standard" -> "Standard Plan";
                        case "free" -> "Free Plan";
                        default -> planKey;
                    });
                    return plans.save(created);
                });

        OrganizationEntity org = new OrganizationEntity();
        org.setName(request.organizationName().trim());
        org.setDescription("Registered via auth form");
        org.setAdminUserId(user.getId());
        org.setPlan(plan);
        org.setPaymentPeriod(request.paymentPeriod().trim().toLowerCase());
        org.setStatus("Active");
        org.setAddress(request.address().trim());
        org.setCountry(request.country().trim());
        org.setNextBillingAt(calculateNextBillingDate(org.getPaymentPeriod()));
        organizations.save(org);

        String displayName = (user.getFirstName() + " " + user.getLastName()).trim();
        authMailService.sendEmailVerification(user.getEmail(), displayName, user.getEmailVerificationToken());

        return new RegisterResponse(
                "Registration successful. Please check your email to confirm your account before signing in.",
                user.getEmail(),
                true
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserEntity user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        String password = request.password() == null ? "" : request.password().trim();
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        if (!user.isEnabled()) {
            throw new BadCredentialsException("Account is disabled");
        }
        if (!user.isEmailVerified()) {
            throw new BadCredentialsException(
                    "Please verify your email before signing in. Check your inbox for the confirmation link."
            );
        }

        String schoolId = null;
        if (user.getRole() == UserRole.ADMIN_SCHOOL) {
            schoolId = organizations
                    .findByAdminUserId(user.getId())
                    .map(OrganizationEntity::getId)
                    .orElse(null);
        }

        return buildAuthResponse(user, schoolId);
    }

    @Transactional
    public VerifyEmailResponse verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new BadCredentialsException("Verification token is required");
        }
        UserEntity user = users.findByEmailVerificationToken(token.trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired verification link"));
        if (user.getEmailVerificationExpiresAt() == null
                || user.getEmailVerificationExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Verification link has expired. Request a new confirmation email.");
        }
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiresAt(null);
        users.save(user);
        return new VerifyEmailResponse("Email confirmed. You can now sign in.", true);
    }

    @Transactional
    public MessageResponse resendVerificationEmail(ResendVerificationRequest request) {
        users.findByEmailIgnoreCase(request.email().trim()).ifPresent(user -> {
            if (!user.isEmailVerified()) {
                issueEmailVerificationToken(user);
                users.save(user);
                String displayName = (user.getFirstName() + " " + user.getLastName()).trim();
                authMailService.sendEmailVerification(user.getEmail(), displayName, user.getEmailVerificationToken());
            }
        });
        return genericEmailSentMessage();
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        users.findByEmailIgnoreCase(request.email().trim()).ifPresent(user -> {
            user.setPasswordResetToken(newSecureToken());
            user.setPasswordResetExpiresAt(Instant.now().plusSeconds(PASSWORD_RESET_TTL_HOURS * 3600));
            users.save(user);
            String displayName = (user.getFirstName() + " " + user.getLastName()).trim();
            authMailService.sendPasswordReset(user.getEmail(), displayName, user.getPasswordResetToken());
        });
        return genericEmailSentMessage();
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        UserEntity user = users.findByPasswordResetToken(request.token().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired reset link"));
        if (user.getPasswordResetExpiresAt() == null
                || user.getPasswordResetExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Reset link has expired. Request a new password reset email.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
        users.save(user);
        return new MessageResponse("Password updated. You can now sign in with your new password.");
    }

    private void issueEmailVerificationToken(UserEntity user) {
        user.setEmailVerificationToken(newSecureToken());
        user.setEmailVerificationExpiresAt(
                Instant.now().plusSeconds(EMAIL_VERIFICATION_TTL_HOURS * 3600)
        );
    }

    private static String newSecureToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static MessageResponse genericEmailSentMessage() {
        return new MessageResponse(
                "If an account exists for this email, we sent instructions. Please check your inbox."
        );
    }

    private AuthResponse buildAuthResponse(UserEntity user, String schoolId) {
        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        AuthUserResponse userResponse = new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                schoolId,
                null,
                user.getCreatedAt() == null ? null : user.getCreatedAt().toString(),
                user.getUpdatedAt() == null ? null : user.getUpdatedAt().toString()
        );
        return new AuthResponse(userResponse, accessToken, refreshToken, jwtService.getExpirationSeconds());
    }

    private LocalDateTime calculateNextBillingDate(String period) {
        return switch (period) {
            case "yearly" -> LocalDateTime.now().plusYears(1);
            case "quarterly" -> LocalDateTime.now().plusMonths(3);
            default -> LocalDateTime.now().plusMonths(1);
        };
    }
}
