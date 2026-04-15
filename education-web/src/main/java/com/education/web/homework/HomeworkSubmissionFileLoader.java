package com.education.web.homework;

import com.education.web.homework.dto.HomeworkFileDownload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Читання файлу здачі з каталогу {@code app.homework-upload.dir} з перевіркою path traversal.
 */
@Component
public class HomeworkSubmissionFileLoader {

    private final Path uploadRoot;

    public HomeworkSubmissionFileLoader(
            @Value("${app.homework-upload.dir:uploads/homework}") String uploadDir
    ) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public Path getUploadRoot() {
        return uploadRoot;
    }

    public HomeworkFileDownload loadForSubmission(HomeworkPortalSubmissionEntity s) {
        if (!HomeworkAttachmentPolicy.hasDownloadableAttachment(s)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No file attached to this submission");
        }
        Path file = uploadRoot.resolve(s.getStoragePath());
        if (!file.normalize().startsWith(uploadRoot)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid path");
        }
        try {
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File missing on server");
            }
            String name = s.getFileName() != null ? s.getFileName().trim() : "homework";
            return new HomeworkFileDownload(resource, name, file, s.getContentType());
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
