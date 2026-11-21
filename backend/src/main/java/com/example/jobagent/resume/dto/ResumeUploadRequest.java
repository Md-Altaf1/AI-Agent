package com.example.jobagent.resume.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class ResumeUploadRequest {

    @NotBlank(message = "Resume label is required")
    @Size(max = 100, message = "Label cannot exceed 100 characters")
    private String label;

    @NotNull(message = "Resume file is required")
    private MultipartFile file;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}