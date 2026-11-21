package ru.skillfactory.eduplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateAssignmentRequest(
        @NotBlank(message = "Название задания обязательно")
        @Size(max = 200, message = "Название задания не должно превышать 200 символов")
        String title,

        String description,

        LocalDateTime dueDate,

        @NotNull(message = "Необходимо указать максимальный балл")
        @Positive(message = "Максимальный балл должен быть положительным")
        Integer maxScore
) {
}