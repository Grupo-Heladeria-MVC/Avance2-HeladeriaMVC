package com.utp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SecurityLoggingService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityLoggingService.class);

    public void logRegistrationAttempt(String email, String ipAddress, boolean success) {
        if (success) {
            logger.info("Registration attempt for email: {} from IP: {} - SUCCESS",
                    sanitizeEmail(email), ipAddress);
        } else {
            logger.warn("Registration attempt for email: {} from IP: {} - FAILED",
                    sanitizeEmail(email), ipAddress);
        }
    }

    public void logUpdateAttempt(Integer userId, String ip, boolean success) {
        if (success) {
            logger.info("Usuario {} actualizó datos exitosamente desde IP: {}", userId, ip);
        } else {
            logger.warn("INTENTO FALLIDO: Usuario {} intentó actualizar datos desde IP: {}", userId, ip);
        }
    }

    public void logFailedPasswordAttempt(String username, String ip) {
        logger.warn("CONTRASEÑA INCORRECTA: Usuario {} desde IP: {}", username, ip);
    }

    public void logSuspiciousActivity(String ipAddress, String reason) {
        logger.warn("SUSPICIOUS ACTIVITY detected from IP: {} - Reason: {}", ipAddress, reason);
    }

    private String sanitizeEmail(String email) {
        if (email == null || email.length() < 3) {
            return "***";
        }
        return email.substring(0, 2) + "***@"
                + (email.contains("@") ? email.split("@")[1] : "***");
    }
}
