package ru.skillfactory.eduplatform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateEnrollmentRequest(
        @NotNull(message = "Необходимо указать ID студента") @Positive(message = "ID студента должно быть положительным числом")
        Long studentId,
        @NotNull(message = "Необходимо указать ID курса")
        @Positive(message = "ID курса должно быть положительным числом")
        Long courseId
) {
}
