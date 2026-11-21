package com.example.jobagent.resume.dto;

import com.example.jobagent.resume.entity.ResumeVersion;

import java.time.Instant;

public class ResumeUploadResponse {

    private Long id;
    private Long resumeId;
    private Integer versionNo;
    private String originalFilename;
    private String contentType;
    private Integer fileSizeBytes;
    private String sha256;
    private String parseStatus;
    private Instant createdAt;

    public static ResumeUploadResponse fromEntity(ResumeVersion entity) {

        ResumeUploadResponse response = new ResumeUploadResponse();

        response.id = entity.getId();
        response.resumeId = entity.getResume().getId();
        response.versionNo = entity.getVersionNo();
        response.originalFilename = entity.getOriginalFilename();
        response.contentType = entity.getContentType();
        response.fileSizeBytes = entity.getFileSizeBytes();
        response.sha256 = entity.getSha256();
        response.parseStatus = entity.getParseStatus().name();
        response.createdAt = entity.getCreatedAt();

        return response;
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