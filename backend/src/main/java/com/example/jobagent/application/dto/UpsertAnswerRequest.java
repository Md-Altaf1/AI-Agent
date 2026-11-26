package com.example.jobagent.application.dto;

import com.example.jobagent.application.entity.AnswerSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpsertAnswerRequest(
        @NotBlank(message = "fieldKey is required")
        @Size(max = 100)
        String fieldKey,

        @NotBlank(message = "questionText is required")
        @Size(max = 500)
        String questionText,

        String answerText,

        @NotNull(message = "answerSource is required")
        AnswerSource answerSource,

        boolean approved
) {
}