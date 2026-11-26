package com.example.jobagent.matching.controller;

import com.example.jobagent.matching.dto.JobMatchRequest;
import com.example.jobagent.matching.dto.JobMatchResponse;
import com.example.jobagent.matching.service.JobMatchingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class JobMatchController {

    private final JobMatchingService matchingService;

    public JobMatchController(JobMatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<JobMatchResponse> evaluateMatch(
            Authentication authentication,
            @Valid @RequestBody JobMatchRequest request) {
        return ResponseEntity.ok(
                matchingService.evaluateMatch(
                        authentication.getName(),
                        request.jobId(),
                        request.resumeVersionId()
                )
        );
    }

    @GetMapping("/resume-versions/{resumeVersionId}")
    public ResponseEntity<List<JobMatchResponse>> getMatchesForResumeVersion(
            Authentication authentication,
            @PathVariable Long resumeVersionId) {
        return ResponseEntity.ok(
                matchingService.getMatchesForResumeVersion(authentication.getName(), resumeVersionId)
        );
    }
}