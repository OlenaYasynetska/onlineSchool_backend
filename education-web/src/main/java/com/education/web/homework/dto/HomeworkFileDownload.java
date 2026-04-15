package com.education.web.homework.dto;

import org.springframework.core.io.Resource;

import java.nio.file.Path;

/**
 * Файл здачі ДЗ з диска + ім’я для Content-Disposition.
 *
 * @param storedContentType значення з БД (може бути null); інакше — probeContentType / розширення.
 */
public record HomeworkFileDownload(
        Resource resource,
        String downloadFileName,
        Path resolvedPath,
        String storedContentType
) {}
