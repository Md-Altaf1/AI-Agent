package com.example.jobagent.job.dto;

import com.example.jobagent.job.entity.EmploymentType;
import com.example.jobagent.job.entity.WorkMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class JobImportRequest {

    @Size(max = 255)
    private String externalId;

    @NotBlank(message = "Job title is required")
    @Size(max = 255)
    private String title;

    @NotBlank(message = "Company name is required")
    @Size(max = 255)
    private String companyName;

    @NotBlank(message = "Location text is required")
    @Size(max = 255)
    private String locationText;

    @Size(max = 100)
    private String locationCity;

    private EmploymentType employmentType = EmploymentType.UNKNOWN;

    private WorkMode workMode = WorkMode.UNKNOWN;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "50.0")
    private BigDecimal minExperienceYears;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "50.0")
    private BigDecimal maxExperienceYears;

    @Min(0)
    private Integer salaryMin;

    @Min(0)
    private Integer salaryMax;

    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code (e.g. INR, USD)")
    private String salaryCurrency;

    @NotBlank(message = "Job description is required")
    private String description;

    @NotBlank(message = "Application URL is required")
    @Size(max = 2048)
    private String applicationUrl;

    private Instant postedAt;

    @Valid
    private List<JobSkillDto> skills = new ArrayList<>();

    // Getters & Setters
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

    public List<JobSkillDto> getSkills() { return skills; }
    public void setSkills(List<JobSkillDto> skills) { this.skills = skills; }
}