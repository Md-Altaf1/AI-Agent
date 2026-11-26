package com.example.jobagent.application.dto;

import com.example.jobagent.application.entity.ApplicationStatus;

import java.util.Map;

public record ApplicationSummaryResponse(
        long totalApplications,
        Map<ApplicationStatus, Long> countsByStatus
) {
}