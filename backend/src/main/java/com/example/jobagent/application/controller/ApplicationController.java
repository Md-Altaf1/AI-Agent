package com.example.jobagent.application.controller;

import com.example.jobagent.application.dto.ApplicationAnswerResponse;
import com.example.jobagent.application.dto.ApplicationEventResponse;
import com.example.jobagent.application.dto.ApplicationResponse;
import com.example.jobagent.application.dto.CreateApplicationRequest;
import com.example.jobagent.application.dto.UpdateApplicationStatusRequest;
import com.example.jobagent.application.dto.UpsertAnswerRequest;
import com.example.jobagent.application.entity.ApplicationStatus;
import com.example.jobagent.application.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            Authentication authentication,
            @Valid @RequestBody CreateApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.createApplication(authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<Page<ApplicationResponse>> getApplications(
            Authentication authentication,
            @RequestParam(required = false) ApplicationStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                applicationService.getApplications(authentication.getName(), status, pageable)
        );
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            Authentication authentication,
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(
                applicationService.getApplicationById(authentication.getName(), applicationId)
        );
    }

    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody UpdateApplicationStatusRequest request) {
        return ResponseEntity.ok(
                applicationService.updateStatus(authentication.getName(), applicationId, request)
        );
    }

    @PutMapping("/{applicationId}/answers")
    public ResponseEntity<ApplicationAnswerResponse> upsertAnswer(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody UpsertAnswerRequest request) {
        return ResponseEntity.ok(
                applicationService.upsertAnswer(authentication.getName(), applicationId, request)
        );
    }

    @GetMapping("/{applicationId}/answers")
    public ResponseEntity<List<ApplicationAnswerResponse>> getAnswers(
            Authentication authentication,
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(
                applicationService.getAnswers(authentication.getName(), applicationId)
        );
    }

    @GetMapping("/{applicationId}/events")
    public ResponseEntity<List<ApplicationEventResponse>> getEvents(
            Authentication authentication,
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(
                applicationService.getEvents(authentication.getName(), applicationId)
        );
    }
}