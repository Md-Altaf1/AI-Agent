package com.example.jobagent.matching.service;

import com.example.jobagent.auth.entity.User;
import com.example.jobagent.auth.repository.UserRepository;
import com.example.jobagent.common.config.exception.ResourceNotFoundException;
import com.example.jobagent.job.entity.Job;
import com.example.jobagent.job.entity.JobSkill;
import com.example.jobagent.job.entity.WorkMode;
import com.example.jobagent.job.repository.JobRepository;
import com.example.jobagent.job.repository.JobSkillRepository;
import com.example.jobagent.matching.dto.JobMatchResponse;
import com.example.jobagent.matching.entity.JobMatch;
import com.example.jobagent.matching.entity.MatchRecommendation;
import com.example.jobagent.matching.repository.JobMatchRepository;
import com.example.jobagent.profile.entity.UserPreference;
import com.example.jobagent.profile.entity.UserSkill;
import com.example.jobagent.profile.repository.UserPreferenceRepository;
import com.example.jobagent.profile.repository.UserSkillRepository;
import com.example.jobagent.resume.entity.ResumeVersion;
import com.example.jobagent.resume.repository.ResumeVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JobMatchingService {

    private final JobMatchRepository jobMatchRepository;
    private final JobRepository jobRepository;
    private final JobSkillRepository jobSkillRepository;
    private final ResumeVersionRepository resumeVersionRepository;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    public JobMatchingService(
            JobMatchRepository jobMatchRepository,
            JobRepository jobRepository,
            JobSkillRepository jobSkillRepository,
            ResumeVersionRepository resumeVersionRepository,
            UserRepository userRepository,
            UserSkillRepository userSkillRepository,
            UserPreferenceRepository userPreferenceRepository) {
        this.jobMatchRepository = jobMatchRepository;
        this.jobRepository = jobRepository;
        this.jobSkillRepository = jobSkillRepository;
        this.resumeVersionRepository = resumeVersionRepository;
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
        this.userPreferenceRepository = userPreferenceRepository;
    }

    @Transactional
    public JobMatchResponse evaluateMatch(String email, Long jobId, Long resumeVersionId) {
        User user = findUserByEmail(email);

        ResumeVersion resumeVersion = resumeVersionRepository.findById(resumeVersionId)
                .filter(rv -> rv.getResume().getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found: " + resumeVersionId));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        // 1. Return cached match if job content has not changed
        JobMatch existing = jobMatchRepository
                .findByResumeVersionIdAndJobId(resumeVersionId, jobId)
                .orElse(null);

        if (existing != null && existing.getContentHash().equals(job.getContentHash())) {
            return JobMatchResponse.fromEntity(existing);
        }

        // 2. Stage 1: Hard preference rules
        UserPreference pref = userPreferenceRepository.findById(user.getId()).orElse(null);
        List<String> ruleReasons = evaluateHardRules(job, pref);
        boolean ruleEligible = ruleReasons.isEmpty();

        // 3. Stage 2: Weighted skill overlap scoring (required = 2, optional = 1)
        List<JobSkill> jobSkills = jobSkillRepository.findByIdJobId(job.getId());
        Set<String> declaredSkills = userSkillRepository.findByUserIdOrderBySkillNameAsc(user.getId())
                .stream()
                .map(UserSkill::getSkillName)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        String resumeTextLower = resumeVersion.getExtractedText() != null
                ? resumeVersion.getExtractedText().toLowerCase(Locale.ROOT)
                : "";

        List<String> matchingSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        int totalWeight = 0;
        int matchedWeight = 0;

        for (JobSkill jobSkill : jobSkills) {
            int weight = jobSkill.isRequired() ? 2 : 1;
            totalWeight += weight;

            String skillLower = jobSkill.getSkillName().toLowerCase(Locale.ROOT);
            boolean hasSkill = declaredSkills.contains(skillLower) || resumeTextLower.contains(skillLower);

            if (hasSkill) {
                matchedWeight += weight;
                matchingSkills.add(jobSkill.getSkillName());
            } else {
                missingSkills.add(jobSkill.getSkillName());
            }
        }

        int score = (totalWeight == 0)
                ? 50
                : (int) Math.round((matchedWeight * 100.0) / totalWeight);

        int minScore = (pref != null) ? (int) pref.getMinMatchScore() : 60;
        boolean finalEligible;
        MatchRecommendation recommendation;

        if (!ruleEligible) {
            finalEligible = false;
            recommendation = MatchRecommendation.SKIP;
        } else if (score >= minScore) {
            finalEligible = true;
            recommendation = MatchRecommendation.APPLY;
        } else if (score >= Math.max(minScore - 15, 0)) {
            finalEligible = false;
            recommendation = MatchRecommendation.APPLY_WITH_CAUTION;
        } else {
            finalEligible = false;
            recommendation = MatchRecommendation.SKIP;
        }

        // 4. Persist or update match record
        JobMatch match = (existing != null)
                ? existing
                : new JobMatch(resumeVersion, job, job.getContentHash(), ruleEligible);

        match.setContentHash(job.getContentHash());
        match.setRuleEligible(ruleEligible);
        match.setRuleReasons(ruleReasons);
        match.setMatchScore((byte) score);
        match.setEligible(finalEligible);
        match.setRecommendation(recommendation);
        match.setMatchingSkills(matchingSkills);
        match.setMissingSkills(missingSkills);

        return JobMatchResponse.fromEntity(jobMatchRepository.save(match));
    }

    @Transactional(readOnly = true)
    public List<JobMatchResponse> getMatchesForResumeVersion(String email, Long resumeVersionId) {
        User user = findUserByEmail(email);

        resumeVersionRepository.findById(resumeVersionId)
                .filter(rv -> rv.getResume().getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found: " + resumeVersionId));

        return jobMatchRepository.findByResumeVersionIdOrderByMatchScoreDesc(resumeVersionId)
                .stream()
                .map(JobMatchResponse::fromEntity)
                .toList();
    }

    private List<String> evaluateHardRules(Job job, UserPreference pref) {
        List<String> reasons = new ArrayList<>();
        if (pref == null) {
            return reasons;
        }

        String titleLower = job.getTitle().toLowerCase(Locale.ROOT);
        for (String excluded : pref.getExcludedTitleKeywords()) {
            if (excluded != null && !excluded.isBlank()
                    && titleLower.contains(excluded.trim().toLowerCase(Locale.ROOT))) {
                reasons.add("Title contains excluded keyword: " + excluded.trim());
            }
        }

        if (job.getMinExperienceYears() != null && pref.getMaxRequiredExperienceYears() != null
                && job.getMinExperienceYears().compareTo(pref.getMaxRequiredExperienceYears()) > 0) {
            reasons.add("Minimum required experience (" + job.getMinExperienceYears()
                    + " yrs) exceeds preference (" + pref.getMaxRequiredExperienceYears() + " yrs)");
        }

        if (pref.getPreferredLocations() != null && !pref.getPreferredLocations().isEmpty()) {
            String locationLower = (job.getLocationText() + " " + (job.getLocationCity() != null ? job.getLocationCity() : ""))
                    .toLowerCase(Locale.ROOT);

            boolean locationMatched = pref.getPreferredLocations().stream()
                    .filter(loc -> loc != null && !loc.isBlank())
                    .map(loc -> loc.trim().toLowerCase(Locale.ROOT))
                    .anyMatch(loc -> locationLower.contains(loc)
                            || (loc.equals("remote") && job.getWorkMode() == WorkMode.REMOTE));

            if (!locationMatched) {
                reasons.add("Job location (" + job.getLocationText() + ") does not match preferred locations");
            }
        }

        if (pref.getEmploymentTypes() != null && !pref.getEmploymentTypes().isEmpty()) {
            boolean typeMatched = pref.getEmploymentTypes().stream()
                    .anyMatch(t -> t != null && t.equalsIgnoreCase(job.getEmploymentType().name()));
            if (!typeMatched) {
                reasons.add("Employment type (" + job.getEmploymentType() + ") not in preferences");
            }
        }

        return reasons;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    @Transactional
    public int evaluateAllJobsForUser(String email) {
        User user = findUserByEmail(email);
        List<ResumeVersion> resumeVersions = resumeVersionRepository.findByResumeUserId(user.getId());
        if (resumeVersions.isEmpty()) {
            return 0;
        }

        List<Job> jobs = jobRepository.findAll();
        int evaluatedCount = 0;

        for (ResumeVersion rv : resumeVersions) {
            for (Job job : jobs) {
                evaluateMatch(email, job.getId(), rv.getId());
                evaluatedCount++;
            }
        }
        return evaluatedCount;
    }
}