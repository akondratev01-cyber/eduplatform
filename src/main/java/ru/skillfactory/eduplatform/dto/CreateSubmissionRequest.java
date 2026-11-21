package ru.skillfactory.eduplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSubmissionRequest(
        @NotNull(message = "Необходимо указать ID студента")
        @Positive(message = "ID студента должно быть положительным числом")
        Long studentId,
        @NotBlank(message = "Решение не может быть пустым")
        String content
) {
}
