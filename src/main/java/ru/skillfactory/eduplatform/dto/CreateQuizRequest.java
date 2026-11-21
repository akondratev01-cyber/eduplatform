package ru.skillfactory.eduplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateQuizRequest(
        @NotBlank(message = "Название теста обязательно")
        @Size(max = 200, message = "Название теста не должно превышать 200 символов")
        String title,

        @NotNull(message = "Необходимо указать лимит времени")
        @Positive(message = "Лимит времени должен быть положительным")
        Integer timeLimit
) {
}