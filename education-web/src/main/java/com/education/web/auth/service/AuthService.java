package com.education.web.auth.service;

import com.education.web.auth.dto.AuthResponse;
import com.education.web.auth.dto.AuthUserResponse;
import com.education.web.auth.dto.LoginRequest;
import com.education.web.auth.dto.RegisterRequest;
import com.education.web.auth.model.OrganizationEntity;
import com.education.web.auth.model.SubscriptionPlanEntity;
import com.education.web.auth.model.UserEntity;
import com.education.web.auth.model.UserRole;
import com.education.web.auth.repository.OrganizationJpaRepository;
import com.education.web.auth.repository.SubscriptionPlanJpaRepository;
import com.education.web.auth.repository.UserJpaRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final UserJpaRepository users;
    private final OrganizationJpaRepository organizations;
    private final SubscriptionPlanJpaRepository plans;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserJpaRepository users,
            OrganizationJpaRepository organizations,
            SubscriptionPlanJpaRepository plans,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.users = users;
        this.organizations = organizations;
        this.plans = plans;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
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
        user = users.save(user);

        String planKey = request.plan().trim().toLowerCase();
        SubscriptionPlanEntity plan = plans.findByPlanKeyIgnoreCase(planKey)
                .orElseGet(() -> {
                    // Если в БД пока нет seed-строк (например, в dev окружении), создаём план на лету.
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
        org = organizations.save(org);

        return buildAuthResponse(user, org.getId());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserEntity user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        // Як і SUPER_ADMIN_PASSWORD у bootstrap — прибираємо випадкові пробіли з копіювання.
        String password = request.password() == null ? "" : request.password().trim();
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        if (!user.isEnabled()) {
            throw new BadCredentialsException("Account is disabled");
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

