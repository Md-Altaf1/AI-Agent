package com.example.jobagent.resume.entity;

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
import java.util.Map;

@Entity
@Table(name = "resume_versions")
public class ResumeVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size_bytes", nullable = false)
    private Integer fileSizeBytes;

    @Column(name = "sha256", nullable = false, columnDefinition = "CHAR(64)")
    private String sha256;

    @Column(name = "storage_key", nullable = false, columnDefinition = "CHAR(36)")
    private String storageKey;

    @Column(name = "extracted_text", columnDefinition = "MEDIUMTEXT")
    private String extractedText;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parsed_profile", columnDefinition = "json")
    private Map<String, Object> parsedProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "parse_status", nullable = false, length = 20)
    private ParseStatus parseStatus = ParseStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ResumeVersion() {
    }

    public ResumeVersion(
            Resume resume,
            Integer versionNo,
            String originalFilename,
            String contentType,
            Integer fileSizeBytes,
            String sha256,
            String storageKey,
            String extractedText
    ) {
        this.resume = resume;
        this.versionNo = versionNo;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.sha256 = sha256;
        this.storageKey = storageKey;
        this.extractedText = extractedText;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Resume getResume() { return resume; }
    public Integer getVersionNo() { return versionNo; }
    public String getOriginalFilename() { return originalFilename; }
    public String getContentType() { return contentType; }
    public Integer getFileSizeBytes() { return fileSizeBytes; }
    public String getSha256() { return sha256; }
    public String getStorageKey() { return storageKey; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }

    public Map<String, Object> getParsedProfile() { return parsedProfile; }
    public void setParsedProfile(Map<String, Object> parsedProfile) { this.parsedProfile = parsedProfile; }

    public ParseStatus getParseStatus() { return parseStatus; }
    public void setParseStatus(ParseStatus parseStatus) { this.parseStatus = parseStatus; }

    public Instant getCreatedAt() { return createdAt; }
}