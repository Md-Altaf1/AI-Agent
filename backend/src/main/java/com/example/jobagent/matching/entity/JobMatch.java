package com.example.jobagent.matching.entity;

import com.example.jobagent.job.entity.Job;
import com.example.jobagent.resume.entity.ResumeVersion;
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
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_matches")
public class JobMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_version_id", nullable = false)
    private ResumeVersion resumeVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "content_hash", nullable = false, columnDefinition = "CHAR(64)")
    private String contentHash;

    @Column(name = "rule_eligible", nullable = false)
    private boolean ruleEligible;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rule_reasons", columnDefinition = "json")
    private List<String> ruleReasons = new ArrayList<>();

    @Column(name = "match_score")
    private Byte matchScore;

    @Column(name = "eligible")
    private Boolean eligible;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation", length = 25)
    private MatchRecommendation recommendation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matching_skills", columnDefinition = "json")
    private List<String> matchingSkills = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "missing_skills", columnDefinition = "json")
    private List<String> missingSkills = new ArrayList<>();

    @Column(name = "ai_model", length = 100)
    private String aiModel;

    @Column(name = "prompt_version", length = 20)
    private String promptVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected JobMatch() {
    }

    public JobMatch(ResumeVersion resumeVersion, Job job, String contentHash, boolean ruleEligible) {
        this.resumeVersion = resumeVersion;
        this.job = job;
        this.contentHash = contentHash;
        this.ruleEligible = ruleEligible;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public ResumeVersion getResumeVersion() { return resumeVersion; }
    public Job getJob() { return job; }

    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }

    public boolean isRuleEligible() { return ruleEligible; }
    public void setRuleEligible(boolean ruleEligible) { this.ruleEligible = ruleEligible; }

    public List<String> getRuleReasons() { return ruleReasons; }
    public void setRuleReasons(List<String> ruleReasons) { this.ruleReasons = ruleReasons; }

    public Byte getMatchScore() { return matchScore; }
    public void setMatchScore(Byte matchScore) { this.matchScore = matchScore; }

    public Boolean getEligible() { return eligible; }
    public void setEligible(Boolean eligible) { this.eligible = eligible; }

    public MatchRecommendation getRecommendation() { return recommendation; }
    public void setRecommendation(MatchRecommendation recommendation) { this.recommendation = recommendation; }

    public List<String> getMatchingSkills() { return matchingSkills; }
    public void setMatchingSkills(List<String> matchingSkills) { this.matchingSkills = matchingSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }

    public String getPromptVersion() { return promptVersion; }
    public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }

    public Instant getCreatedAt() { return createdAt; }
}