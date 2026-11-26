package com.example.jobagent.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateApplicationRequest(
        @NotNull(message = "jobId is required")
        Long jobId,

        Long resumeVersionId,

        @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
        String notes
) {
}