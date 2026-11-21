package com.example.jobagent.resume.dto;

import com.example.jobagent.resume.entity.Resume;

import java.time.Instant;

public class ResumeResponse {

    private Long id;
    private String label;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer latestVersionNo;

    public ResumeResponse() {
    }

    public ResumeResponse(
            Long id,
            String label,
            Instant createdAt,
            Instant updatedAt,
            Integer latestVersionNo
    ) {
        this.id = id;
        this.label = label;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.latestVersionNo = latestVersionNo;
    }

    public static ResumeResponse fromEntity(Resume resume) {

        return new ResumeResponse(
                resume.getId(),
                resume.getLabel(),
                resume.getCreatedAt(),
                resume.getUpdatedAt(),
                null
        );
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Integer getLatestVersionNo() {
        return latestVersionNo;
    }

    public void setLatestVersionNo(Integer latestVersionNo) {
        this.latestVersionNo = latestVersionNo;
    }
}