package com.example.jobagent.job.service;

import com.example.jobagent.job.entity.EmploymentType;
import com.example.jobagent.job.entity.Job;
import com.example.jobagent.job.entity.JobSkill;
import com.example.jobagent.job.entity.JobSource;
import com.example.jobagent.job.entity.JobSourceType;
import com.example.jobagent.job.entity.WorkMode;
import com.example.jobagent.job.ingestion.FetchedJobPosting;
import com.example.jobagent.job.ingestion.JobSourceConnector;
import com.example.jobagent.job.repository.JobRepository;
import com.example.jobagent.job.repository.JobSkillRepository;
import com.example.jobagent.job.repository.JobSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class JobIngestionService {

    private static final Logger log = LoggerFactory.getLogger(JobIngestionService.class);

    private final List<JobSourceConnector> connectors;
    private final JobSourceRepository jobSourceRepository;
    private final JobRepository jobRepository;
    private final JobSkillRepository jobSkillRepository;

    public JobIngestionService(
            List<JobSourceConnector> connectors,
            JobSourceRepository jobSourceRepository,
            JobRepository jobRepository,
            JobSkillRepository jobSkillRepository) {
        this.connectors = connectors;
        this.jobSourceRepository = jobSourceRepository;
        this.jobRepository = jobRepository;
        this.jobSkillRepository = jobSkillRepository;
    }

    public record IngestionSummary(int insertedCount, int updatedCount, int unchangedCount, int failedConnectors) {
    }

    public IngestionSummary ingestAllSources() {
        int totalInserted = 0;
        int totalUpdated = 0;
        int totalUnchanged = 0;
        int failedConnectors = 0;

        for (JobSourceConnector connector : connectors) {
            try {
                List<FetchedJobPosting> postings = connector.fetchJobs();
                IngestionSummary summary = ingestPostings(connector.sourceCode(), postings);
                totalInserted += summary.insertedCount();
                totalUpdated += summary.updatedCount();
                totalUnchanged += summary.unchangedCount();
                log.info("Connector {} processed: inserted={}, updated={}, unchanged={}",
                        connector.sourceCode(), summary.insertedCount(), summary.updatedCount(), summary.unchangedCount());
            } catch (Exception ex) {
                failedConnectors++;
                log.error("Ingestion failed for connector {}: {}", connector.sourceCode(), ex.getMessage());
            }
        }

        return new IngestionSummary(totalInserted, totalUpdated, totalUnchanged, failedConnectors);
    }

    @Transactional
    public IngestionSummary ingestPostings(String sourceCode, List<FetchedJobPosting> postings) {
        if (postings == null || postings.isEmpty()) {
            return new IngestionSummary(0, 0, 0, 0);
        }

        JobSource source = resolveOrCreateSource(sourceCode);
        Instant batchTime = Instant.now();

        int inserted = 0;
        int updated = 0;
        int unchanged = 0;

        for (FetchedJobPosting posting : postings) {
            String dedupeKey = buildDedupeKey(sourceCode, posting);
            String contentHash = buildContentHash(posting);

            Optional<Job> existingOpt = jobRepository.findByDedupeKey(dedupeKey);

            if (existingOpt.isEmpty()) {
                Job job = createJobEntity(source, posting, dedupeKey, contentHash, batchTime);
                Job savedJob = jobRepository.save(job);
                saveSkillsForJob(savedJob, posting.skills());
                inserted++;
            } else {
                Job existing = existingOpt.get();
                existing.setLastSeenAt(batchTime);
                existing.setActive(true);

                if (!contentHash.equals(existing.getContentHash())) {
                    updateJobFields(existing, posting, contentHash);
                    Job savedJob = jobRepository.save(existing);
                    saveSkillsForJob(savedJob, posting.skills());
                    updated++;
                } else {
                    jobRepository.save(existing);
                    unchanged++;
                }
            }
        }

        return new IngestionSummary(inserted, updated, unchanged, 0);
    }

    private Job createJobEntity(
            JobSource source,
            FetchedJobPosting posting,
            String dedupeKey,
            String contentHash,
            Instant seenAt) {
        Job job = new Job(
                source,
                posting.title(),
                posting.companyName(),
                posting.locationText(),
                posting.description(),
                posting.applicationUrl(),
                dedupeKey,
                contentHash
        );
        job.setExternalId(posting.externalId());
        job.setLocationCity(posting.locationCity());
        job.setEmploymentType(posting.employmentType() != null ? posting.employmentType() : EmploymentType.UNKNOWN);
        job.setWorkMode(posting.workMode() != null ? posting.workMode() : WorkMode.UNKNOWN);
        job.setPostedAt(posting.postedAt());
        job.setActive(true);
        job.setLastSeenAt(seenAt);
        return job;
    }

    private void updateJobFields(Job existing, FetchedJobPosting posting, String contentHash) {
        existing.setTitle(posting.title());
        existing.setCompanyName(posting.companyName());
        existing.setLocationText(posting.locationText());
        existing.setLocationCity(posting.locationCity());
        existing.setEmploymentType(posting.employmentType() != null ? posting.employmentType() : EmploymentType.UNKNOWN);
        existing.setWorkMode(posting.workMode() != null ? posting.workMode() : WorkMode.UNKNOWN);
        existing.setDescription(posting.description());
        existing.setApplicationUrl(posting.applicationUrl());
        existing.setPostedAt(posting.postedAt());
        existing.setContentHash(contentHash);
    }

    private JobSource resolveOrCreateSource(String sourceCode) {
        return jobSourceRepository.findAll().stream()
                .filter(s -> sourceCode.equalsIgnoreCase(s.getCode()))
                .findFirst()
                .orElseGet(() -> jobSourceRepository.save(
                        new JobSource(sourceCode, sourceCode, resolveSourceType())
                ));
    }

    private JobSourceType resolveSourceType() {
        // Apne JobSourceType.java enum mein maujood exact constant yahan rakhein (e.g., JobSourceType.JOB_BOARD)
        return JobSourceType.values()[0];
    }

    private void saveSkillsForJob(Job savedJob, List<String> skills) {
        if (skills == null || skills.isEmpty() || savedJob.getId() == null) {
            return;
        }
        Set<String> uniqueSkills = new LinkedHashSet<>();
        for (String skill : skills) {
            if (skill != null && !skill.isBlank()) {
                uniqueSkills.add(skill.trim());
            }
        }
        if (uniqueSkills.isEmpty()) {
            return;
        }
        List<JobSkill> skillEntities = new ArrayList<>(uniqueSkills.size());
        for (String skillName : uniqueSkills) {
            skillEntities.add(new JobSkill(savedJob, skillName, false));
        }
        jobSkillRepository.saveAll(skillEntities);
    }

    private String buildDedupeKey(String sourceCode, FetchedJobPosting posting) {
        String identity = (posting.externalId() != null && !posting.externalId().isBlank())
                ? sourceCode.toUpperCase(Locale.ROOT) + ":" + posting.externalId().trim()
                : sourceCode.toUpperCase(Locale.ROOT) + ":"
                + normalize(posting.title()) + "|"
                + normalize(posting.companyName()) + "|"
                + normalize(posting.applicationUrl());
        return sha256Hex(identity);
    }

    private String buildContentHash(FetchedJobPosting posting) {
        EmploymentType empType = posting.employmentType() != null ? posting.employmentType() : EmploymentType.UNKNOWN;
        WorkMode mode = posting.workMode() != null ? posting.workMode() : WorkMode.UNKNOWN;

        String payload = normalize(posting.title()) + "|"
                + normalize(posting.companyName()) + "|"
                + normalize(posting.locationText()) + "|"
                + empType.name() + "|"
                + mode.name() + "|"
                + normalize(posting.description()) + "|"
                + normalize(posting.applicationUrl());
        return sha256Hex(payload);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }
}