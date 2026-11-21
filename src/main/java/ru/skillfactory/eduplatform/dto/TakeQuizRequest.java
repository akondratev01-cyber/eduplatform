package ru.skillfactory.eduplatform.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Map;

public record TakeQuizRequest(
        @NotNull(message = "Необходимо указать ID студента")
        @Positive(message = "ID студента должен быть положительным")
        Long studentId,

        @NotEmpty(message = "Необходимо передать ответы на вопросы")
        Map<Long, List<Long>> answers
) {
}