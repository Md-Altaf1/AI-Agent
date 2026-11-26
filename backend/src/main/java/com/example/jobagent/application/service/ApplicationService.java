package com.example.jobagent.application.service;

import com.example.jobagent.application.dto.*;
import com.example.jobagent.application.entity.ActorType;
import com.example.jobagent.application.entity.Application;
import com.example.jobagent.application.entity.ApplicationAnswer;
import com.example.jobagent.application.entity.ApplicationEvent;
import com.example.jobagent.application.entity.ApplicationEventType;
import com.example.jobagent.application.entity.ApplicationStatus;
import com.example.jobagent.application.repository.ApplicationAnswerRepository;
import com.example.jobagent.application.repository.ApplicationEventRepository;
import com.example.jobagent.application.repository.ApplicationRepository;
import com.example.jobagent.auth.entity.User;
import com.example.jobagent.auth.repository.UserRepository;
import com.example.jobagent.common.config.exception.ResourceNotFoundException;
import com.example.jobagent.job.entity.Job;
import com.example.jobagent.job.repository.JobRepository;
import com.example.jobagent.matching.entity.JobMatch;
import com.example.jobagent.matching.repository.JobMatchRepository;
import com.example.jobagent.resume.entity.ResumeVersion;
import com.example.jobagent.resume.repository.ResumeVersionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationAnswerRepository answerRepository;
    private final ApplicationEventRepository eventRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ResumeVersionRepository resumeVersionRepository;
    private final JobMatchRepository jobMatchRepository;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            ApplicationAnswerRepository answerRepository,
            ApplicationEventRepository eventRepository,
            UserRepository userRepository,
            JobRepository jobRepository,
            ResumeVersionRepository resumeVersionRepository,
            JobMatchRepository jobMatchRepository) {
        this.applicationRepository = applicationRepository;
        this.answerRepository = answerRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.resumeVersionRepository = resumeVersionRepository;
        this.jobMatchRepository = jobMatchRepository;
    }

    @Transactional
    public ApplicationResponse createApplication(String email, CreateApplicationRequest request) {
        User user = findUserByEmail(email);

        if (applicationRepository.findByUserIdAndJobId(user.getId(), request.jobId()).isPresent()) {
            throw new IllegalArgumentException("Application already exists for this job");
        }

        Job job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + request.jobId()));

        ResumeVersion resumeVersion = null;
        Byte matchScore = null;

        if (request.resumeVersionId() != null) {
            resumeVersion = resumeVersionRepository.findById(request.resumeVersionId())
                    .filter(rv -> rv.getResume().getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Resume version not found: " + request.resumeVersionId()));

            matchScore = jobMatchRepository.findByResumeVersionIdAndJobId(resumeVersion.getId(), job.getId())
                    .map(JobMatch::getMatchScore)
                    .orElse(null);
        }

        Application application = new Application(
                user,
                job,
                resumeVersion,
                ApplicationStatus.DISCOVERED,
                matchScore
        );
        if (request.notes() != null && !request.notes().isBlank()) {
            application.setNotes(request.notes().trim());
        }

        Application saved = applicationRepository.save(application);

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("jobId", job.getId());
        if (resumeVersion != null) {
            details.put("resumeVersionId", resumeVersion.getId());
        }
        if (matchScore != null) {
            details.put("matchScore", matchScore.intValue());
        }

        recordEvent(saved, ApplicationEventType.CREATED, null, saved.getStatus().name(), ActorType.USER, details);

        return ApplicationResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplications(String email, ApplicationStatus status, Pageable pageable) {
        User user = findUserByEmail(email);

        Page<Application> page = (status != null)
                ? applicationRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(user.getId(), status, pageable)
                : applicationRepository.findByUserIdOrderByUpdatedAtDesc(user.getId(), pageable);

        return page.map(ApplicationResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(String email, Long applicationId) {
        User user = findUserByEmail(email);
        Application application = findOwnedApplication(applicationId, user.getId());
        return ApplicationResponse.fromEntity(application);
    }

    @Transactional
    public ApplicationResponse updateStatus(String email, Long applicationId, UpdateApplicationStatusRequest request) {
        User user = findUserByEmail(email);
        Application application = findOwnedApplication(applicationId, user.getId());

        ApplicationStatus previousStatus = application.getStatus();
        ApplicationStatus newStatus = request.status();

        // Enforce strict state machine transitions
        if (previousStatus != newStatus && !previousStatus.canTransitionTo(newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status transition from " + previousStatus + " to " + newStatus
            );
        }

        application.setStatus(newStatus);

        if (newStatus == ApplicationStatus.SUBMITTED && application.getAppliedAt() == null) {
            application.setAppliedAt(Instant.now());
        }
        if (request.interviewAt() != null) {
            application.setInterviewAt(request.interviewAt());
        }
        if (request.hrContactName() != null) {
            application.setHrContactName(request.hrContactName().trim());
        }
        if (request.hrContactEmail() != null) {
            application.setHrContactEmail(request.hrContactEmail().trim());
        }
        if (request.notes() != null) {
            application.setNotes(request.notes().trim());
        }

        Application saved = applicationRepository.save(application);

        ApplicationEventType eventType;
        if (previousStatus == newStatus) {
            eventType = ApplicationEventType.NOTE_ADDED;
        } else if (newStatus == ApplicationStatus.APPROVED) {
            eventType = ApplicationEventType.APPROVAL_GRANTED;
        } else {
            eventType = ApplicationEventType.STATUS_CHANGED;
        }

        Map<String, Object> details = new LinkedHashMap<>();
        if (request.notes() != null && !request.notes().isBlank()) {
            details.put("notes", request.notes().trim());
        }

        recordEvent(
                saved,
                eventType,
                previousStatus != newStatus ? previousStatus.name() : null,
                previousStatus != newStatus ? newStatus.name() : null,
                ActorType.USER,
                details.isEmpty() ? null : details
        );

        return ApplicationResponse.fromEntity(saved);
    }

    @Transactional
    public ApplicationAnswerResponse upsertAnswer(String email, Long applicationId, UpsertAnswerRequest request) {
        User user = findUserByEmail(email);
        Application application = findOwnedApplication(applicationId, user.getId());

        String cleanFieldKey = request.fieldKey().trim();

        ApplicationAnswer answer = answerRepository
                .findByApplicationIdAndFieldKey(application.getId(), cleanFieldKey)
                .map(existing -> {
                    existing.setQuestionText(request.questionText().trim());
                    existing.setAnswerText(request.answerText());
                    existing.setAnswerSource(request.answerSource());
                    existing.setApproved(request.approved());
                    return existing;
                })
                .orElseGet(() -> {
                    ApplicationAnswer created = new ApplicationAnswer(
                            application,
                            cleanFieldKey,
                            request.questionText().trim(),
                            request.answerText(),
                            request.answerSource()
                    );
                    created.setApproved(request.approved());
                    return created;
                });

        ApplicationAnswer saved = answerRepository.save(answer);

        Map<String, Object> details = Map.of(
                "fieldKey", saved.getFieldKey(),
                "answerSource", saved.getAnswerSource().name(),
                "approved", saved.isApproved()
        );
        recordEvent(application, ApplicationEventType.ANSWER_EDITED, null, null, ActorType.USER, details);

        return ApplicationAnswerResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationAnswerResponse> getAnswers(String email, Long applicationId) {
        User user = findUserByEmail(email);
        Application application = findOwnedApplication(applicationId, user.getId());

        return answerRepository.findByApplicationIdOrderByIdAsc(application.getId())
                .stream()
                .map(ApplicationAnswerResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationEventResponse> getEvents(String email, Long applicationId) {
        User user = findUserByEmail(email);
        Application application = findOwnedApplication(applicationId, user.getId());

        return eventRepository.findByApplicationIdOrderByCreatedAtAsc(application.getId())
                .stream()
                .map(ApplicationEventResponse::fromEntity)
                .toList();
    }

    private void recordEvent(
            Application application,
            ApplicationEventType eventType,
            String fromStatus,
            String toStatus,
            ActorType actorType,
            Map<String, Object> details) {
        ApplicationEvent event = new ApplicationEvent(
                application,
                eventType,
                fromStatus,
                toStatus,
                actorType,
                details
        );
        eventRepository.save(event);
    }

    private Application findOwnedApplication(Long applicationId, Long userId) {
        return applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    @Transactional(readOnly = true)
    public ApplicationSummaryResponse getSummary(String email) {
        User user = findUserByEmail(email);

        List<ApplicationRepository.StatusCountProjection> rows =
                applicationRepository.countByUserIdGroupByStatus(user.getId());

        Map<ApplicationStatus, Long> counts = new EnumMap<>(ApplicationStatus.class);
        long total = 0;

        for (ApplicationRepository.StatusCountProjection row : rows) {
            counts.put(row.getStatus(), row.getCount());
            total += row.getCount();
        }

        return new ApplicationSummaryResponse(total, counts);
    }
}