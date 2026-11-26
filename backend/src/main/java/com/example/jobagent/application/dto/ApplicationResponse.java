package com.example.jobagent.application.dto;

import com.example.jobagent.application.entity.Application;
import com.example.jobagent.application.entity.ApplicationStatus;

import java.time.Instant;

public record ApplicationResponse(
        Long id,
        Long jobId,
        String jobTitle,
        String companyName,
        Long resumeVersionId,
        ApplicationStatus status,
        Integer matchScore,
        Instant appliedAt,
        Instant interviewAt,
        String hrContactName,
        String hrContactEmail,
        String notes,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
    public static ApplicationResponse fromEntity(Application app) {
        return new ApplicationResponse(
                app.getId(),
                app.getJob().getId(),
                app.getJob().getTitle(),
                app.getJob().getCompanyName(),
                app.getResumeVersion() != null ? app.getResumeVersion().getId() : null,
                app.getStatus(),
                app.getMatchScore() != null ? app.getMatchScore().intValue() : null,
                app.getAppliedAt(),
                app.getInterviewAt(),
                app.getHrContactName(),
                app.getHrContactEmail(),
                app.getNotes(),
                app.getVersion(),
                app.getCreatedAt(),
                app.getUpdatedAt()
        );
    }
}