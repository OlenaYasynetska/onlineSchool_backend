package com.education.web.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Запрошення на email для учнів та викладачів. Без SMTP — лише WARN у лог.
 */
@Service
public class AccountInvitationMailService {

    private static final Logger log = LoggerFactory.getLogger(AccountInvitationMailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public AccountInvitationMailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    /**
     * @param logRoleLabel для логу: "student" / "teacher"
     * @return true якщо лист відправлено через SMTP
     */
    public boolean sendInvite(
            String toEmail,
            String displayName,
            String plainPassword,
            String loginHintEmail,
            String logRoleLabel
    ) {
        String loginUrl = frontendBaseUrl.replaceAll("/+$", "") + "/auth/login";
        String subject = "Your school account — login details";
        String body = """
                Hello%s,

                An account has been created for you on the education platform.

                Login page: %s
                Email (login): %s
                Password: %s

                Please sign in and change your password after the first login if prompted.

                —
                School administration
                """
                .formatted(
                        displayName.isBlank() ? "" : " " + displayName,
                        loginUrl,
                        loginHintEmail,
                        plainPassword
                );

        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn(
                    "SMTP not configured (spring.mail.host); {} invitation not sent to {}. Body:\n{}",
                    logRoleLabel,
                    toEmail,
                    body
            );
            return false;
        }
        if (mailFrom == null || mailFrom.isBlank()) {
            log.warn("spring.mail.username is empty; cannot send mail to {}", toEmail);
            log.debug("Invitation body:\n{}", body);
            return false;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(toEmail);
            msg.setSubject(subject);
            msg.setText(body);
            sender.send(msg);
            log.info("{} invitation email sent to {}", logRoleLabel, toEmail);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send {} invitation email to {}", logRoleLabel, toEmail, ex);
            return false;
        }
    }
}
