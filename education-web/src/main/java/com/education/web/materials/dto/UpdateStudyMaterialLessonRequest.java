package com.education.web.materials.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateStudyMaterialLessonRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 2048)
    private String issuuEmbedUrl;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIssuuEmbedUrl() {
        return issuuEmbedUrl;
    }

    public void setIssuuEmbedUrl(String issuuEmbedUrl) {
        this.issuuEmbedUrl = issuuEmbedUrl;
    }
}
