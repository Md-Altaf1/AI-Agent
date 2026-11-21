package com.example.jobagent.profile.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class UserPreferenceRequest {

    @NotNull(message = "Target titles list cannot be null")
    private List<String> targetTitles = new ArrayList<>();

    @NotNull(message = "Preferred locations list cannot be null")
    private List<String> preferredLocations = new ArrayList<>();

    @NotNull(message = "Employment types list cannot be null")
    private List<String> employmentTypes = new ArrayList<>();

    @NotNull(message = "Excluded title keywords list cannot be null")
    private List<String> excludedTitleKeywords = new ArrayList<>();

    @NotNull
    @DecimalMin(value = "0.0", message = "Max experience must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Max experience cannot exceed 10.0")
    private BigDecimal maxRequiredExperienceYears = new BigDecimal("2.0");

    @NotNull
    @Min(value = 0, message = "Min match score must be between 0 and 100")
    @Max(value = 100, message = "Min match score must be between 0 and 100")
    private Integer minMatchScore = 60;

    @NotNull
    @Min(value = 1, message = "Max jobs per search must be at least 1")
    @Max(value = 500, message = "Max jobs per search cannot exceed 500")
    private Integer maxJobsPerSearch = 50;

    private boolean externalAiEnabled = true;

    public List<String> getTargetTitles() { return targetTitles; }
    public void setTargetTitles(List<String> targetTitles) { this.targetTitles = targetTitles; }

    public List<String> getPreferredLocations() { return preferredLocations; }
    public void setPreferredLocations(List<String> preferredLocations) { this.preferredLocations = preferredLocations; }

    public List<String> getEmploymentTypes() { return employmentTypes; }
    public void setEmploymentTypes(List<String> employmentTypes) { this.employmentTypes = employmentTypes; }

    public List<String> getExcludedTitleKeywords() { return excludedTitleKeywords; }
    public void setExcludedTitleKeywords(List<String> excludedTitleKeywords) { this.excludedTitleKeywords = excludedTitleKeywords; }

    public BigDecimal getMaxRequiredExperienceYears() { return maxRequiredExperienceYears; }
    public void setMaxRequiredExperienceYears(BigDecimal maxRequiredExperienceYears) { this.maxRequiredExperienceYears = maxRequiredExperienceYears; }

    public Integer getMinMatchScore() { return minMatchScore; }
    public void setMinMatchScore(Integer minMatchScore) { this.minMatchScore = minMatchScore; }

    public Integer getMaxJobsPerSearch() { return maxJobsPerSearch; }
    public void setMaxJobsPerSearch(Integer maxJobsPerSearch) { this.maxJobsPerSearch = maxJobsPerSearch; }

    public boolean isExternalAiEnabled() { return externalAiEnabled; }
    public void setExternalAiEnabled(boolean externalAiEnabled) { this.externalAiEnabled = externalAiEnabled; }
}