package com.education.web.homework;

import org.springframework.http.MediaType;

import java.nio.file.Files;
import java.nio.file.Path;

/** Визначення Content-Type для видачі файлу в браузер / завантаження. */
public final class HomeworkFileMediaTypes {

    private HomeworkFileMediaTypes() {}

    public static MediaType resolve(Path file, String storedContentType, String fileName) {
        if (storedContentType != null && !storedContentType.isBlank()) {
            try {
                return MediaType.parseMediaType(storedContentType);
            } catch (Exception ignored) {
                // fall through
            }
        }
        try {
            String probed = Files.probeContentType(file);
            if (probed != null && !probed.isBlank()) {
                return MediaType.parseMediaType(probed);
            }
        } catch (Exception ignored) {
            // fall through
        }
        if (fileName != null) {
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".pdf")) {
                return MediaType.APPLICATION_PDF;
            }
            if (lower.endsWith(".png")) {
                return MediaType.IMAGE_PNG;
            }
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                return MediaType.IMAGE_JPEG;
            }
            if (lower.endsWith(".gif")) {
                return MediaType.IMAGE_GIF;
            }
            if (lower.endsWith(".webp")) {
                return new MediaType("image", "webp");
            }
            if (lower.endsWith(".txt")) {
                return MediaType.TEXT_PLAIN;
            }
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
