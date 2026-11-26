package com.example.jobagent.application.dto;

import com.example.jobagent.application.entity.AnswerSource;
import com.example.jobagent.application.entity.ApplicationAnswer;

import java.time.Instant;

public record ApplicationAnswerResponse(
        Long id,
        Long applicationId,
        String fieldKey,
        String questionText,
        String answerText,
        AnswerSource answerSource,
        boolean approved,
        Instant createdAt,
        Instant updatedAt
) {
    public static ApplicationAnswerResponse fromEntity(ApplicationAnswer answer) {
        return new ApplicationAnswerResponse(
                answer.getId(),
                answer.getApplication().getId(),
                answer.getFieldKey(),
                answer.getQuestionText(),
                answer.getAnswerText(),
                answer.getAnswerSource(),
                answer.isApproved(),
                answer.getCreatedAt(),
                answer.getUpdatedAt()
        );
    }
}