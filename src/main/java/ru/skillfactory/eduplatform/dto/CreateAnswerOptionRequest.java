package ru.skillfactory.eduplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAnswerOptionRequest(
        @NotBlank(message = "Текст варианта ответа обязателен")
        @Size(max = 1000, message = "Текст варианта ответа не должен превышать 1000 символов")
        String text,

        boolean correct
) {
}