package com.example.jobagent.application.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationStatusTest {

    @Test
    void discoveredShouldAllowValidTransitionsOnly() {
        assertTrue(ApplicationStatus.DISCOVERED.canTransitionTo(ApplicationStatus.ELIGIBLE));
        assertTrue(ApplicationStatus.DISCOVERED.canTransitionTo(ApplicationStatus.READY_FOR_REVIEW));
        assertTrue(ApplicationStatus.DISCOVERED.canTransitionTo(ApplicationStatus.WITHDRAWN));

        assertFalse(ApplicationStatus.DISCOVERED.canTransitionTo(ApplicationStatus.SUBMITTED));
        assertFalse(ApplicationStatus.DISCOVERED.canTransitionTo(ApplicationStatus.INTERVIEW_SCHEDULED));
    }

    @Test
    void approvedShouldTransitionToSubmittedOrWithdrawnOnly() {
        assertTrue(ApplicationStatus.APPROVED.canTransitionTo(ApplicationStatus.SUBMITTED));
        assertTrue(ApplicationStatus.APPROVED.canTransitionTo(ApplicationStatus.WITHDRAWN));

        assertFalse(ApplicationStatus.APPROVED.canTransitionTo(ApplicationStatus.INTERVIEW_SCHEDULED));
        assertFalse(ApplicationStatus.APPROVED.canTransitionTo(ApplicationStatus.DISCOVERED));
    }

    @Test
    void terminalStatesShouldNotAllowAnyTransition() {
        for (ApplicationStatus target : ApplicationStatus.values()) {
            assertFalse(ApplicationStatus.REJECTED.canTransitionTo(target));
            assertFalse(ApplicationStatus.WITHDRAWN.canTransitionTo(target));
        }
    }

    @Test
    void nullTargetStatusShouldReturnFalse() {
        assertFalse(ApplicationStatus.DISCOVERED.canTransitionTo(null));
    }
}