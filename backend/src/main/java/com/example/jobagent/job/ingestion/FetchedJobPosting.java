package com.example.jobagent.job.ingestion;

import com.example.jobagent.job.entity.EmploymentType;
import com.example.jobagent.job.entity.WorkMode;

import java.time.Instant;
import java.util.List;

public record FetchedJobPosting(
        String externalId,
        String title,
        String companyName,
        String locationText,
        String locationCity,
        EmploymentType employmentType,
        WorkMode workMode,
        String description,
        String applicationUrl,
        Instant postedAt,
        List<String> skills
) {
}