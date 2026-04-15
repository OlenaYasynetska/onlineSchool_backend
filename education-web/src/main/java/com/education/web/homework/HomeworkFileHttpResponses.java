package com.education.web.homework;

import com.education.web.homework.dto.HomeworkFileDownload;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

public final class HomeworkFileHttpResponses {

    private HomeworkFileHttpResponses() {}

    public static ResponseEntity<Resource> toResponse(HomeworkFileDownload fd, boolean inline) {
        String safe = fd.downloadFileName() != null
                ? fd.downloadFileName().replace("\"", "'")
                : "homework";
        MediaType mt = HomeworkFileMediaTypes.resolve(
                fd.resolvedPath(),
                fd.storedContentType(),
                fd.downloadFileName()
        );
        ContentDisposition cd = inline
                ? ContentDisposition.inline().filename(safe, StandardCharsets.UTF_8).build()
                : ContentDisposition.attachment().filename(safe, StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .contentType(mt)
                .body(fd.resource());
    }
}
