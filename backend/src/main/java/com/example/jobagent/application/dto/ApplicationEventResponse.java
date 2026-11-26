package com.example.jobagent.application.dto;

import com.example.jobagent.application.entity.ActorType;
import com.example.jobagent.application.entity.ApplicationEvent;
import com.example.jobagent.application.entity.ApplicationEventType;

import java.time.Instant;
import java.util.Map;

public record ApplicationEventResponse(
        Long id,
        Long applicationId,
        ApplicationEventType eventType,
        String fromStatus,
        String toStatus,
        ActorType actorType,
        Map<String, Object> details,
        Instant createdAt
) {
    public static ApplicationEventResponse fromEntity(ApplicationEvent event) {
        return new ApplicationEventResponse(
                event.getId(),
                event.getApplication().getId(),
                event.getEventType(),
                event.getFromStatus(),
                event.getToStatus(),
                event.getActorType(),
                event.getDetails(),
                event.getCreatedAt()
        );
    }
}