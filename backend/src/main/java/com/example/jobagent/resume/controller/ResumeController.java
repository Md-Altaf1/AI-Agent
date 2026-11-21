package com.example.jobagent.resume.controller;

import com.example.jobagent.resume.dto.ResumeFileDownload;
import com.example.jobagent.resume.dto.ResumeResponse;
import com.example.jobagent.resume.dto.ResumeUploadRequest;
import com.example.jobagent.resume.dto.ResumeUploadResponse;
import com.example.jobagent.resume.dto.ResumeVersionResponse;
import com.example.jobagent.resume.service.ResumeService;

import jakarta.validation.Valid;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    // 1. Upload Resume
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeUploadResponse> uploadResume(
            Authentication authentication,
            @Valid @ModelAttribute ResumeUploadRequest request) {

        ResumeUploadResponse response = resumeService.uploadResume(
                authentication.getName(),
                request.getLabel(),
                request.getFile()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 2. Get All Resumes
    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getAllResumes(
            Authentication authentication) {

        List<ResumeResponse> resumes =
                resumeService.getAllResumes(authentication.getName());

        return ResponseEntity.ok(resumes);
    }

    // 3. Get Resume Version History
    @GetMapping("/{resumeId}/versions")
    public ResponseEntity<List<ResumeVersionResponse>> getResumeVersions(
            Authentication authentication,
            @PathVariable Long resumeId) {

        List<ResumeVersionResponse> versions =
                resumeService.getResumeVersions(
                        authentication.getName(),
                        resumeId
                );

        return ResponseEntity.ok(versions);
    }

    // 4. Get Specific Resume Version
    @GetMapping("/{resumeId}/versions/{versionId}")
    public ResponseEntity<ResumeVersionResponse> getResumeVersion(
            Authentication authentication,
            @PathVariable Long resumeId,
            @PathVariable Long versionId) {

        ResumeVersionResponse response =
                resumeService.getResumeVersion(
                        authentication.getName(),
                        resumeId,
                        versionId
                );

        return ResponseEntity.ok(response);
    }

    // 5. Download Resume Version
    @GetMapping("/{resumeId}/versions/{versionId}/download")
    public ResponseEntity<Resource> downloadResumeVersion(
            Authentication authentication,
            @PathVariable Long resumeId,
            @PathVariable Long versionId) {

        ResumeFileDownload download =
                resumeService.downloadResumeVersion(
                        authentication.getName(),
                        resumeId,
                        versionId
                );

        Resource resource =
                new FileSystemResource(download.filePath());

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(download.contentType())
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(download.originalFilename())
                                .build()
                                .toString()
                )
                .body(resource);
    }
}