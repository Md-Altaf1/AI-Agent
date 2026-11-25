package com.example.jobagent.job.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private JobSource source;

    @Column(name = "external_id", length = 255)
    private String externalId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "location_text", nullable = false, length = 255)
    private String locationText;

    @Column(name = "location_city", length = 100)
    private String locationCity;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 20)
    private EmploymentType employmentType = EmploymentType.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", nullable = false, length = 20)
    private WorkMode workMode = WorkMode.UNKNOWN;

    @Column(name = "min_experience_years", precision = 3, scale = 1)
    private BigDecimal minExperienceYears;

    @Column(name = "max_experience_years", precision = 3, scale = 1)
    private BigDecimal maxExperienceYears;

    @Column(name = "salary_min")
    private Integer salaryMin;

    @Column(name = "salary_max")
    private Integer salaryMax;

    @Column(name = "salary_currency", columnDefinition = "CHAR(3)")
    private String salaryCurrency;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String description;

    @Column(name = "application_url", nullable = false, length = 2048)
    private String applicationUrl;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(name = "dedupe_key", nullable = false, columnDefinition = "CHAR(64)")
    private String dedupeKey;

    @Column(name = "content_hash", nullable = false, columnDefinition = "CHAR(64)")
    private String contentHash;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "discovered_at", nullable = false, updatable = false)
    private Instant discoveredAt;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Job() {
    }

    public Job(
            JobSource source,
            String title,
            String companyName,
            String locationText,
            String description,
            String applicationUrl,
            String dedupeKey,
            String contentHash) {
        this.source = source;
        this.title = title;
        this.companyName = companyName;
        this.locationText = locationText;
        this.description = description;
        this.applicationUrl = applicationUrl;
        this.dedupeKey = dedupeKey;
        this.contentHash = contentHash;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.discoveredAt = now;
        this.lastSeenAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public JobSource getSource() { return source; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getLocationText() { return locationText; }
    public void setLocationText(String locationText) { this.locationText = locationText; }

    public String getLocationCity() { return locationCity; }
    public void setLocationCity(String locationCity) { this.locationCity = locationCity; }

    public EmploymentType getEmploymentType() { return employmentType; }
    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType; }

    public WorkMode getWorkMode() { return workMode; }
    public void setWorkMode(WorkMode workMode) { this.workMode = workMode; }

    public BigDecimal getMinExperienceYears() { return minExperienceYears; }
    public void setMinExperienceYears(BigDecimal minExperienceYears) { this.minExperienceYears = minExperienceYears; }

    public BigDecimal getMaxExperienceYears() { return maxExperienceYears; }
    public void setMaxExperienceYears(BigDecimal maxExperienceYears) { this.maxExperienceYears = maxExperienceYears; }

    public Integer getSalaryMin() { return salaryMin; }
    public void setSalaryMin(Integer salaryMin) { this.salaryMin = salaryMin; }

    public Integer getSalaryMax() { return salaryMax; }
    public void setSalaryMax(Integer salaryMax) { this.salaryMax = salaryMax; }

    public String getSalaryCurrency() { return salaryCurrency; }
    public void setSalaryCurrency(String salaryCurrency) { this.salaryCurrency = salaryCurrency; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getApplicationUrl() { return applicationUrl; }
    public void setApplicationUrl(String applicationUrl) { this.applicationUrl = applicationUrl; }

    public Instant getPostedAt() { return postedAt; }
    public void setPostedAt(Instant postedAt) { this.postedAt = postedAt; }

    public String getDedupeKey() { return dedupeKey; }
    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Instant getDiscoveredAt() { return discoveredAt; }
    public Instant getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(Instant lastSeenAt) { this.lastSeenAt = lastSeenAt; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}