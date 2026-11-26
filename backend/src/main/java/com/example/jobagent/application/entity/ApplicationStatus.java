package com.example.jobagent.application.entity;

import java.util.Set;

public enum ApplicationStatus {
    DISCOVERED,
    ELIGIBLE,
    REJECTED_BY_FILTER,
    READY_FOR_REVIEW,
    APPROVED,
    SUBMITTED,
    UNDER_REVIEW,
    SHORTLISTED,
    INTERVIEW_SCHEDULED,
    REJECTED,
    WITHDRAWN;

    public Set<ApplicationStatus> allowedNextStatuses() {
        return switch (this) {
            case DISCOVERED -> Set.of(ELIGIBLE, REJECTED_BY_FILTER, READY_FOR_REVIEW, WITHDRAWN);
            case ELIGIBLE -> Set.of(READY_FOR_REVIEW, APPROVED, WITHDRAWN);
            case REJECTED_BY_FILTER -> Set.of(ELIGIBLE, READY_FOR_REVIEW);
            case READY_FOR_REVIEW -> Set.of(APPROVED, REJECTED, WITHDRAWN);
            case APPROVED -> Set.of(SUBMITTED, WITHDRAWN);
            case SUBMITTED -> Set.of(UNDER_REVIEW, SHORTLISTED, INTERVIEW_SCHEDULED, REJECTED, WITHDRAWN);
            case UNDER_REVIEW -> Set.of(SHORTLISTED, INTERVIEW_SCHEDULED, REJECTED, WITHDRAWN);
            case SHORTLISTED -> Set.of(INTERVIEW_SCHEDULED, REJECTED, WITHDRAWN);
            case INTERVIEW_SCHEDULED -> Set.of(REJECTED, WITHDRAWN);
            case REJECTED, WITHDRAWN -> Set.of(); // Terminal states
        };
    }

    public boolean canTransitionTo(ApplicationStatus nextStatus) {
        return nextStatus != null && allowedNextStatuses().contains(nextStatus);
    }
}