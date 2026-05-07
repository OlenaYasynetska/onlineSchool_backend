package com.education.web.materials;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates HTTPS embed URLs that target Issuu (user-supplied iframe {@code src}).
 */
public final class IssuuEmbedUrlValidator {

    private static final Pattern IFRAME_SRC = Pattern.compile(
            "src\\s*=\\s*([\"'])([^\"']+)\\1",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private IssuuEmbedUrlValidator() {}

    /**
     * @param raw nullable; blank clears the embed
     * @return normalized URL string or {@code null} if cleared
     * @throws IllegalArgumentException if non-blank but not a permitted Issuu HTTPS URL
     */
    public static String normalizeOrThrow(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String candidate = extractSrcIfIframeSnippet(raw.trim());
        URI uri;
        try {
            uri = URI.create(candidate);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL");
        }
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("Only https Issuu embed URLs are allowed");
        }
        String host = uri.getHost();
        if (host == null) {
            throw new IllegalArgumentException("Invalid URL host");
        }
        String h = host.toLowerCase(Locale.ROOT);
        if (!h.equals("issuu.com") && !h.endsWith(".issuu.com")) {
            throw new IllegalArgumentException("URL must be on issuu.com");
        }
        return uri.toString();
    }

    private static String extractSrcIfIframeSnippet(String s) {
        Matcher m = IFRAME_SRC.matcher(s);
        if (m.find()) {
            return m.group(2).trim();
        }
        return s;
    }
}
