package com.example.jobagent.application.dto;

import com.example.jobagent.application.entity.ApplicationStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateApplicationStatusRequest(
        @NotNull(message = "status is required")
        ApplicationStatus status,

        Instant interviewAt,

        @Size(max = 120)
        String hrContactName,

        @Email(message = "Invalid HR contact email")
        @Size(max = 255)
        String hrContactEmail,

        @Size(max = 2000)
        String notes
) {
}