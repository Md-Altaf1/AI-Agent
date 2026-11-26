package com.example.jobagent.matching.dto;

import jakarta.validation.constraints.NotNull;

public record JobMatchRequest(
        @NotNull(message = "jobId is required")
        Long jobId,

        @NotNull(message = "resumeVersionId is required")
        Long resumeVersionId
) {
}