package com.example.jobagent.application.service;

import com.example.jobagent.application.dto.ApplicationResponse;
import com.example.jobagent.application.dto.CreateApplicationRequest;
import com.example.jobagent.application.dto.UpdateApplicationStatusRequest;
import com.example.jobagent.application.entity.Application;
import com.example.jobagent.application.entity.ApplicationEvent;
import com.example.jobagent.application.entity.ApplicationEventType;
import com.example.jobagent.application.entity.ApplicationStatus;
import com.example.jobagent.application.repository.ApplicationAnswerRepository;
import com.example.jobagent.application.repository.ApplicationEventRepository;
import com.example.jobagent.application.repository.ApplicationRepository;
import com.example.jobagent.auth.entity.User;
import com.example.jobagent.auth.repository.UserRepository;
import com.example.jobagent.job.entity.Job;
import com.example.jobagent.job.repository.JobRepository;
import com.example.jobagent.matching.repository.JobMatchRepository;
import com.example.jobagent.resume.repository.ResumeVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private ApplicationAnswerRepository answerRepository;
    @Mock private ApplicationEventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private JobRepository jobRepository;
    @Mock private ResumeVersionRepository resumeVersionRepository;
    @Mock private JobMatchRepository jobMatchRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private User mockUser;
    private Job mockJob;

    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
        mockJob = mock(Job.class);
    }

    @Test
    void createApplication_shouldSaveApplicationAndRecordCreatedEvent() {
        when(mockUser.getId()).thenReturn(1L);
        when(mockJob.getId()).thenReturn(10L);
        when(mockJob.getTitle()).thenReturn("Java Backend Developer");
        when(mockJob.getCompanyName()).thenReturn("Acme FinTech");

        when(userRepository.findByEmail("altaf@example.com")).thenReturn(Optional.of(mockUser));
        when(applicationRepository.findByUserIdAndJobId(1L, 10L)).thenReturn(Optional.empty());
        when(jobRepository.findById(10L)).thenReturn(Optional.of(mockJob));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateApplicationRequest request = new CreateApplicationRequest(10L, null, "Good fit");

        ApplicationResponse response = applicationService.createApplication("altaf@example.com", request);

        assertEquals(ApplicationStatus.DISCOVERED, response.status());
        assertEquals("Java Backend Developer", response.jobTitle());

        ArgumentCaptor<ApplicationEvent> eventCaptor = ArgumentCaptor.forClass(ApplicationEvent.class);
        verify(eventRepository).save(eventCaptor.capture());
        assertEquals(ApplicationEventType.CREATED, eventCaptor.getValue().getEventType());
        assertEquals("DISCOVERED", eventCaptor.getValue().getToStatus());
    }

    @Test
    void createApplication_shouldThrowWhenDuplicateExists() {
        when(mockUser.getId()).thenReturn(1L);
        when(userRepository.findByEmail("altaf@example.com")).thenReturn(Optional.of(mockUser));
        when(applicationRepository.findByUserIdAndJobId(1L, 10L))
                .thenReturn(Optional.of(mock(Application.class)));

        CreateApplicationRequest request = new CreateApplicationRequest(10L, null, null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.createApplication("altaf@example.com", request)
        );

        assertEquals("Application already exists for this job", ex.getMessage());
        verify(applicationRepository, never()).save(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateStatus_shouldRejectInvalidTransitionWithoutSaving() {
        when(mockUser.getId()).thenReturn(1L);
        Application existing = new Application(mockUser, mockJob, null, ApplicationStatus.APPROVED, (byte) 85);

        when(userRepository.findByEmail("altaf@example.com")).thenReturn(Optional.of(mockUser));
        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(existing));

        UpdateApplicationStatusRequest request = new UpdateApplicationStatusRequest(
                ApplicationStatus.INTERVIEW_SCHEDULED, null, null, null, null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.updateStatus("altaf@example.com", 100L, request)
        );

        verify(applicationRepository, never()).save(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateStatus_toSubmitted_shouldSetAppliedAtAndRecordEvent() {
        when(mockUser.getId()).thenReturn(1L);
        when(mockJob.getId()).thenReturn(10L);
        when(mockJob.getTitle()).thenReturn("Java Backend Developer");
        when(mockJob.getCompanyName()).thenReturn("Acme FinTech");

        Application existing = new Application(mockUser, mockJob, null, ApplicationStatus.APPROVED, (byte) 85);

        when(userRepository.findByEmail("altaf@example.com")).thenReturn(Optional.of(mockUser));
        when(applicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(existing));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateApplicationStatusRequest request = new UpdateApplicationStatusRequest(
                ApplicationStatus.SUBMITTED, null, null, null, "Applied via portal"
        );

        ApplicationResponse response = applicationService.updateStatus("altaf@example.com", 100L, request);

        assertEquals(ApplicationStatus.SUBMITTED, response.status());
        assertNotNull(response.appliedAt());

        ArgumentCaptor<ApplicationEvent> eventCaptor = ArgumentCaptor.forClass(ApplicationEvent.class);
        verify(eventRepository).save(eventCaptor.capture());
        assertEquals(ApplicationEventType.STATUS_CHANGED, eventCaptor.getValue().getEventType());
        assertEquals("APPROVED", eventCaptor.getValue().getFromStatus());
        assertEquals("SUBMITTED", eventCaptor.getValue().getToStatus());
    }
}