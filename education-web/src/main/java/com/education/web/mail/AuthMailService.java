package com.education.web.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Листи підтвердження email та відновлення пароля.
 * Без SMTP — WARN у лог (як {@link AccountInvitationMailService}).
 */
@Service
public class AuthMailService {

    private static final Logger log = LoggerFactory.getLogger(AuthMailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public AuthMailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public boolean sendEmailVerification(String toEmail, String displayName, String token) {
        String verifyUrl = frontendBaseUrl.replaceAll("/+$", "")
                + "/auth/verify-email?token=" + token;
        String subject = "Confirm your email — Owl Tracker";
        String body = """
                Hello%s,

                Thank you for registering on Owl Tracker.

                Please confirm your email address by opening this link:
                %s

                This link expires in 24 hours. If you did not create an account, you can ignore this email.

                —
                Owl Tracker
                """
                .formatted(greeting(displayName), verifyUrl);
        return send(toEmail, subject, body, "email verification");
    }

    public boolean sendPasswordReset(String toEmail, String displayName, String token) {
        String resetUrl = frontendBaseUrl.replaceAll("/+$", "")
                + "/auth/reset-password?token=" + token;
        String subject = "Reset your password — Owl Tracker";
        String body = """
                Hello%s,

                We received a request to reset your password.

                Open this link to choose a new password:
                %s

                This link expires in 1 hour. If you did not request a reset, you can ignore this email.

                —
                Owl Tracker
                """
                .formatted(greeting(displayName), resetUrl);
        return send(toEmail, subject, body, "password reset");
    }

    private static String greeting(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return "";
        }
        return " " + displayName.trim();
    }

    private boolean send(String toEmail, String subject, String body, String logLabel) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("SMTP not configured; {} not sent to {}. Body:\n{}", logLabel, toEmail, body);
            return false;
        }
        if (mailFrom == null || mailFrom.isBlank()) {
            log.warn("spring.mail.username is empty; cannot send {} to {}", logLabel, toEmail);
            log.debug("Mail body:\n{}", body);
            return false;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(toEmail);
            msg.setSubject(subject);
            msg.setText(body);
            sender.send(msg);
            log.info("{} email sent to {}", logLabel, toEmail);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send {} email to {}", logLabel, toEmail, ex);
            return false;
        }
    }
}
