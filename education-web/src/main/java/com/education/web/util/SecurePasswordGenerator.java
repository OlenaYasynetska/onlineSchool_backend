package com.education.web.util;

import java.security.SecureRandom;

/** Генерація тимчасового пароля, якщо адмін не ввів свій. */
public final class SecurePasswordGenerator {

    private static final String CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    private static final SecureRandom RANDOM = new SecureRandom();

    private SecurePasswordGenerator() {}

    public static String random(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
