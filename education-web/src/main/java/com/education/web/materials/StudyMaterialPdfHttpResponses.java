package com.education.web.materials;

import com.education.web.homework.HomeworkFileMediaTypes;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

public final class StudyMaterialPdfHttpResponses {

    private StudyMaterialPdfHttpResponses() {}

    public static ResponseEntity<Resource> pdf(byte[] data, String fileName, String storedContentType, boolean inline) {
        if (data == null) {
            data = new byte[0];
        }
        String safe = fileName != null ? fileName.replace("\"", "'") : "lesson.pdf";
        MediaType mt = HomeworkFileMediaTypes.resolveFromFileMeta(storedContentType, fileName);
        ContentDisposition cd = inline
                ? ContentDisposition.inline().filename(safe, StandardCharsets.UTF_8).build()
                : ContentDisposition.attachment().filename(safe, StandardCharsets.UTF_8).build();
        Resource resource = new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return safe;
            }
        };
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .contentType(mt)
                .body(resource);
    }
}
