package ru.skillfactory.eduplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLessonRequest(
        @NotBlank(message = "Название урока обязательно")
        @Size(max = 255, message = "Название урока не должно превышать 255 символов")
        String title,
        @NotBlank(message = "Содержание урока обязательно")
        String content,
        String videoUrl
) {
}