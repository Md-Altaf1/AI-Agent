package com.example.jobagent.matching.dto;

import com.example.jobagent.matching.entity.JobMatch;
import com.example.jobagent.matching.entity.MatchRecommendation;

import java.time.Instant;
import java.util.List;

public record JobMatchResponse(
        Long id,
        Long jobId,
        String jobTitle,
        String companyName,
        Long resumeVersionId,
        boolean ruleEligible,
        List<String> ruleReasons,
        Integer matchScore,
        Boolean eligible,
        MatchRecommendation recommendation,
        List<String> matchingSkills,
        List<String> missingSkills,
        Instant createdAt
) {
    public static JobMatchResponse fromEntity(JobMatch match) {
        return new JobMatchResponse(
                match.getId(),
                match.getJob().getId(),
                match.getJob().getTitle(),
                match.getJob().getCompanyName(),
                match.getResumeVersion().getId(),
                match.isRuleEligible(),
                match.getRuleReasons(),
                match.getMatchScore() != null ? match.getMatchScore().intValue() : null,
                match.getEligible(),
                match.getRecommendation(),
                match.getMatchingSkills(),
                match.getMissingSkills(),
                match.getCreatedAt()
        );
    }
}