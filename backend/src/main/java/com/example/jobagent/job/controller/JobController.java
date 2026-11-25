package com.example.jobagent.job.controller;

import com.example.jobagent.job.dto.JobImportRequest;
import com.example.jobagent.job.dto.JobResponse;
import com.example.jobagent.job.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/import")
    public ResponseEntity<JobResponse> importJob(@Valid @RequestBody JobImportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.importManualJob(request));
    }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> getActiveJobs(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(jobService.getActiveJobs(pageable));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.getJobById(jobId));
    }
}