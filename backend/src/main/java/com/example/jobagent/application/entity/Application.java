package com.example.jobagent.application.entity;

import com.example.jobagent.auth.entity.User;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_version_id")
    private ResumeVersion resumeVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status = ApplicationStatus.DISCOVERED;

    @Column(name = "match_score")
    private Byte matchScore;

    @Column(name = "applied_at")
    private Instant appliedAt;

    @Column(name = "interview_at")
    private Instant interviewAt;

    @Column(name = "hr_contact_name", length = 120)
    private String hrContactName;

    @Column(name = "hr_contact_email", length = 255)
    private String hrContactEmail;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Application() {
    }

    public Application(User user, Job job, ResumeVersion resumeVersion, ApplicationStatus status, Byte matchScore) {
        this.user = user;
        this.job = job;
        this.resumeVersion = resumeVersion;
        this.status = status;
        this.matchScore = matchScore;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Job getJob() { return job; }

    public ResumeVersion getResumeVersion() { return resumeVersion; }
    public void setResumeVersion(ResumeVersion resumeVersion) { this.resumeVersion = resumeVersion; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public Byte getMatchScore() { return matchScore; }
    public void setMatchScore(Byte matchScore) { this.matchScore = matchScore; }

    public Instant getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Instant appliedAt) { this.appliedAt = appliedAt; }

    public Instant getInterviewAt() { return interviewAt; }
    public void setInterviewAt(Instant interviewAt) { this.interviewAt = interviewAt; }

    public String getHrContactName() { return hrContactName; }
    public void setHrContactName(String hrContactName) { this.hrContactName = hrContactName; }

    public String getHrContactEmail() { return hrContactEmail; }
    public void setHrContactEmail(String hrContactEmail) { this.hrContactEmail = hrContactEmail; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}