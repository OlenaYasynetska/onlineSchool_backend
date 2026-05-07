package com.education.web.materials.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Mutable DTO so Jackson/Spring reliably bind JSON (including optional {@code description}) in all deployments.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateStudyMaterialSetRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 4000)
    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
