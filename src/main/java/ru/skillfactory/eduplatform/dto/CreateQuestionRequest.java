package ru.skillfactory.eduplatform.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateQuestionRequest(
        @NotBlank(message = "Текст вопроса обязателен")
        String text,

        @NotBlank(message = "Необходимо указать тип вопроса")
        String type
) {
}