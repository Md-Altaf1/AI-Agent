package com.example.jobagent.job.dto;

import com.example.jobagent.job.entity.EmploymentType;
import com.example.jobagent.job.entity.Job;
import com.example.jobagent.job.entity.JobSkill;
import com.example.jobagent.job.entity.WorkMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record JobResponse(
        Long id,
        String sourceCode,
        String externalId,
        String title,
        String companyName,
        String locationText,
        String locationCity,
        EmploymentType employmentType,
        WorkMode workMode,
        BigDecimal minExperienceYears,
        BigDecimal maxExperienceYears,
        Integer salaryMin,
        Integer salaryMax,
        String salaryCurrency,
        String description,
        String applicationUrl,
        Instant postedAt,
        boolean active,
        Instant lastSeenAt,
        List<JobSkillDto> skills
) {
    public static JobResponse fromEntity(Job job, List<JobSkill> jobSkills) {
        List<JobSkillDto> skillDtos = jobSkills.stream()
                .map(s -> new JobSkillDto(s.getSkillName(), s.isRequired()))
                .toList();

        return new JobResponse(
                job.getId(),
                job.getSource().getCode(),
                job.getExternalId(),
                job.getTitle(),
                job.getCompanyName(),
                job.getLocationText(),
                job.getLocationCity(),
                job.getEmploymentType(),
                job.getWorkMode(),
                job.getMinExperienceYears(),
                job.getMaxExperienceYears(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getSalaryCurrency(),
                job.getDescription(),
                job.getApplicationUrl(),
                job.getPostedAt(),
                job.isActive(),
                job.getLastSeenAt(),
                skillDtos
        );
    }
}