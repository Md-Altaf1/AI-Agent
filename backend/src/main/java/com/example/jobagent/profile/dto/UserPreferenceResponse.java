package com.example.jobagent.profile.dto;

import com.example.jobagent.profile.entity.UserPreference;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class UserPreferenceResponse {

    private final Long userId;
    private final List<String> targetTitles;
    private final List<String> preferredLocations;
    private final List<String> employmentTypes;
    private final List<String> excludedTitleKeywords;
    private final BigDecimal maxRequiredExperienceYears;
    private final Integer minMatchScore;
    private final Integer maxJobsPerSearch;
    private final boolean externalAiEnabled;
    private final Long preferredResumeId;
    private final Instant updatedAt;

    public UserPreferenceResponse(
            Long userId,
            List<String> targetTitles,
            List<String> preferredLocations,
            List<String> employmentTypes,
            List<String> excludedTitleKeywords,
            BigDecimal maxRequiredExperienceYears,
            Integer minMatchScore,
            Integer maxJobsPerSearch,
            boolean externalAiEnabled,
            Long preferredResumeId,
            Instant updatedAt) {
        this.userId = userId;
        this.targetTitles = targetTitles;
        this.preferredLocations = preferredLocations;
        this.employmentTypes = employmentTypes;
        this.excludedTitleKeywords = excludedTitleKeywords;
        this.maxRequiredExperienceYears = maxRequiredExperienceYears;
        this.minMatchScore = minMatchScore;
        this.maxJobsPerSearch = maxJobsPerSearch;
        this.externalAiEnabled = externalAiEnabled;
        this.preferredResumeId = preferredResumeId;
        this.updatedAt = updatedAt;
    }

    public static UserPreferenceResponse fromEntity(UserPreference pref) {
        return new UserPreferenceResponse(
                pref.getUserId(),
                pref.getTargetTitles(),
                pref.getPreferredLocations(),
                pref.getEmploymentTypes(),
                pref.getExcludedTitleKeywords(),
                pref.getMaxRequiredExperienceYears(),
                (int) pref.getMinMatchScore(),
                (int) pref.getMaxJobsPerSearch(),
                pref.isExternalAiEnabled(),
                pref.getPreferredResumeId(),
                pref.getUpdatedAt()
        );
    }

    public Long getUserId() { return userId; }
    public List<String> getTargetTitles() { return targetTitles; }
    public List<String> getPreferredLocations() { return preferredLocations; }
    public List<String> getEmploymentTypes() { return employmentTypes; }
    public List<String> getExcludedTitleKeywords() { return excludedTitleKeywords; }
    public BigDecimal getMaxRequiredExperienceYears() { return maxRequiredExperienceYears; }
    public Integer getMinMatchScore() { return minMatchScore; }
    public Integer getMaxJobsPerSearch() { return maxJobsPerSearch; }
    public boolean isExternalAiEnabled() { return externalAiEnabled; }
    public Long getPreferredResumeId() { return preferredResumeId; }
    public Instant getUpdatedAt() { return updatedAt; }
}