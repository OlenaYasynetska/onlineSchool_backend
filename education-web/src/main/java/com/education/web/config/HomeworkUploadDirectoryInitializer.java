package com.education.web.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Створює корінь для завантажень ДЗ при старті. Якщо каталог не створити (нема прав / лише читання),
 * застосунок не підніметься — зрозуміліше, ніж помилка лише при відправці файлу.
 */
@Component
public class HomeworkUploadDirectoryInitializer {

    private static final Logger log = LoggerFactory.getLogger(HomeworkUploadDirectoryInitializer.class);

    private final String uploadDir;

    public HomeworkUploadDirectoryInitializer(
            @Value("${app.homework-upload.dir:uploads/homework}") String uploadDir
    ) {
        this.uploadDir = uploadDir;
    }

    @PostConstruct
    public void ensureUploadRootExists() throws IOException {
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
        log.info("Homework upload directory ready: {}", root);
    }
}
