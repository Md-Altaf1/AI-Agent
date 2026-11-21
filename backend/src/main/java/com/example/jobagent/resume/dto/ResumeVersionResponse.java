package com.example.jobagent.resume.dto;

import com.example.jobagent.resume.entity.ResumeVersion;

import java.time.Instant;

public class ResumeVersionResponse {

    private Long id;
    private Long resumeId;
    private Integer versionNo;
    private String originalFilename;
    private String contentType;
    private Integer fileSizeBytes;
    private String sha256;
    private String parseStatus;
    private Instant createdAt;

    public ResumeVersionResponse() {
    }

    public ResumeVersionResponse(
            Long id,
            Long resumeId,
            Integer versionNo,
            String originalFilename,
            String contentType,
            Integer fileSizeBytes,
            String sha256,
            String parseStatus,
            Instant createdAt
    ) {
        this.id = id;
        this.resumeId = resumeId;
        this.versionNo = versionNo;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.sha256 = sha256;
        this.parseStatus = parseStatus;
        this.createdAt = createdAt;
    }

    public static ResumeVersionResponse fromEntity(ResumeVersion entity) {

        return new ResumeVersionResponse(
                entity.getId(),
                entity.getResume().getId(),
                entity.getVersionNo(),
                entity.getOriginalFilename(),
                entity.getContentType(),
                entity.getFileSizeBytes(),
                entity.getSha256(),
                entity.getParseStatus().name(),
                entity.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public Integer getFileSizeBytes() {
        return fileSizeBytes;
    }

    public String getSha256() {
        return sha256;
    }

    public String getParseStatus() {
        return parseStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}