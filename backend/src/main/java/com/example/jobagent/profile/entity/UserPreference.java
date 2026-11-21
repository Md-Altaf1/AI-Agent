package com.example.jobagent.profile.entity;

import com.example.jobagent.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_titles", nullable = false, columnDefinition = "json")
    private List<String> targetTitles = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferred_locations", nullable = false, columnDefinition = "json")
    private List<String> preferredLocations = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "employment_types", nullable = false, columnDefinition = "json")
    private List<String> employmentTypes = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "excluded_title_keywords", nullable = false, columnDefinition = "json")
    private List<String> excludedTitleKeywords = new ArrayList<>();

    @Column(name = "max_required_experience_years", nullable = false, precision = 3, scale = 1)
    private BigDecimal maxRequiredExperienceYears = new BigDecimal("2.0");

    @Column(name = "min_match_score", nullable = false)
    private byte minMatchScore = 60;

    @Column(name = "max_jobs_per_search", nullable = false)
    private short maxJobsPerSearch = 50;

    @Column(name = "external_ai_enabled", nullable = false)
    private boolean externalAiEnabled = true;

    @Column(name = "preferred_resume_id")
    private Long preferredResumeId;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserPreference() {
    }

    public UserPreference(User user) {
        this.user = user;
    }

    @PrePersist
    @PreUpdate
    void onSave() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public User getUser() { return user; }

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

    public byte getMinMatchScore() { return minMatchScore; }
    public void setMinMatchScore(byte minMatchScore) { this.minMatchScore = minMatchScore; }

    public short getMaxJobsPerSearch() { return maxJobsPerSearch; }
    public void setMaxJobsPerSearch(short maxJobsPerSearch) { this.maxJobsPerSearch = maxJobsPerSearch; }

    public boolean isExternalAiEnabled() { return externalAiEnabled; }
    public void setExternalAiEnabled(boolean externalAiEnabled) { this.externalAiEnabled = externalAiEnabled; }

    public Long getPreferredResumeId() { return preferredResumeId; }
    public void setPreferredResumeId(Long preferredResumeId) { this.preferredResumeId = preferredResumeId; }

    public Instant getUpdatedAt() { return updatedAt; }
}