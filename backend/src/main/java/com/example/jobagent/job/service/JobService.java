package com.example.jobagent.job.service;

import com.example.jobagent.common.config.exception.ResourceNotFoundException;
import com.example.jobagent.job.dto.JobImportRequest;
import com.example.jobagent.job.dto.JobResponse;
import com.example.jobagent.job.dto.JobSkillDto;
import com.example.jobagent.job.entity.EmploymentType;
import com.example.jobagent.job.entity.Job;
import com.example.jobagent.job.entity.JobSkill;
import com.example.jobagent.job.entity.JobSource;
import com.example.jobagent.job.entity.WorkMode;
import com.example.jobagent.job.repository.JobRepository;
import com.example.jobagent.job.repository.JobSkillRepository;
import com.example.jobagent.job.repository.JobSourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class JobService {

    private static final String MANUAL_SOURCE_CODE = "MANUAL_IMPORT";

    private final JobRepository jobRepository;
    private final JobSourceRepository sourceRepository;
    private final JobSkillRepository skillRepository;

    public JobService(
            JobRepository jobRepository,
            JobSourceRepository sourceRepository,
            JobSkillRepository skillRepository) {
        this.jobRepository = jobRepository;
        this.sourceRepository = sourceRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional
    public JobResponse importManualJob(JobImportRequest request) {
        validateRanges(request);

        JobSource source = sourceRepository.findByCode(MANUAL_SOURCE_CODE)
                .orElseThrow(() -> new ResourceNotFoundException("Job source not found: " + MANUAL_SOURCE_CODE));

        String cleanTitle = request.getTitle().trim();
        String cleanCompany = request.getCompanyName().trim();
        String cleanLocation = request.getLocationText().trim();
        String cleanUrl = request.getApplicationUrl().trim();
        String cleanDescription = request.getDescription().trim();

        String dedupeKey = sha256(
                cleanCompany.toLowerCase(Locale.ROOT) + "|" +
                        cleanTitle.toLowerCase(Locale.ROOT) + "|" +
                        cleanUrl.toLowerCase(Locale.ROOT)
        );
        String contentHash = sha256(cleanDescription);

        Job job = jobRepository.findByDedupeKey(dedupeKey).orElse(null);

        if (job == null) {
            job = new Job(source, cleanTitle, cleanCompany, cleanLocation, cleanDescription, cleanUrl, dedupeKey, contentHash);
            applyOptionalFields(job, request);
            job = jobRepository.save(job);
            List<JobSkill> savedSkills = replaceJobSkills(job, request.getSkills());
            return JobResponse.fromEntity(job, savedSkills);
        }

        // Existing job: refresh lastSeenAt, and update fields only if description changed
        job.setLastSeenAt(Instant.now());
        job.setActive(true);

        if (!job.getContentHash().equals(contentHash)) {
            job.setDescription(cleanDescription);
            job.setContentHash(contentHash);
            applyOptionalFields(job, request);
            job = jobRepository.save(job);
            List<JobSkill> savedSkills = replaceJobSkills(job, request.getSkills());
            return JobResponse.fromEntity(job, savedSkills);
        }

        job = jobRepository.save(job);
        List<JobSkill> existingSkills = skillRepository.findByIdJobId(job.getId());
        return JobResponse.fromEntity(job, existingSkills);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getActiveJobs(Pageable pageable) {
        return jobRepository.findByIsActiveTrueOrderByPostedAtDesc(pageable)
                .map(job -> JobResponse.fromEntity(job, skillRepository.findByIdJobId(job.getId())));
    }

    @Transactional(readOnly = true)
    public JobResponse getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));
        List<JobSkill> skills = skillRepository.findByIdJobId(job.getId());
        return JobResponse.fromEntity(job, skills);
    }

    private void applyOptionalFields(Job job, JobImportRequest request) {
        job.setExternalId(trimToNull(request.getExternalId()));
        job.setLocationCity(trimToNull(request.getLocationCity()));
        job.setEmploymentType(request.getEmploymentType() != null ? request.getEmploymentType() : EmploymentType.UNKNOWN);
        job.setWorkMode(request.getWorkMode() != null ? request.getWorkMode() : WorkMode.UNKNOWN);
        job.setMinExperienceYears(request.getMinExperienceYears());
        job.setMaxExperienceYears(request.getMaxExperienceYears());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setSalaryCurrency(
                request.getSalaryCurrency() != null ? request.getSalaryCurrency().trim().toUpperCase(Locale.ROOT) : null
        );
        job.setPostedAt(request.getPostedAt() != null ? request.getPostedAt() : Instant.now());
    }

    private List<JobSkill> replaceJobSkills(Job job, List<JobSkillDto> skillDtos) {
        skillRepository.deleteByIdJobId(job.getId());
        if (skillDtos == null || skillDtos.isEmpty()) {
            return List.of();
        }

        // Deduplicate skills case-insensitively before inserting into composite PK table
        Map<String, JobSkillDto> uniqueSkills = new LinkedHashMap<>();
        for (JobSkillDto dto : skillDtos) {
            if (dto.skillName() != null && !dto.skillName().isBlank()) {
                String key = dto.skillName().trim().toLowerCase(Locale.ROOT);
                uniqueSkills.putIfAbsent(key, new JobSkillDto(dto.skillName().trim(), dto.required()));
            }
        }

        List<JobSkill> entities = uniqueSkills.values().stream()
                .map(dto -> new JobSkill(job, dto.skillName(), dto.required()))
                .toList();

        return skillRepository.saveAll(entities);
    }

    private void validateRanges(JobImportRequest request) {
        if (request.getMinExperienceYears() != null && request.getMaxExperienceYears() != null
                && request.getMinExperienceYears().compareTo(request.getMaxExperienceYears()) > 0) {
            throw new IllegalArgumentException("minExperienceYears cannot be greater than maxExperienceYears");
        }
        if (request.getSalaryMin() != null && request.getSalaryMax() != null
                && request.getSalaryMin() > request.getSalaryMax()) {
            throw new IllegalArgumentException("salaryMin cannot be greater than salaryMax");
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}