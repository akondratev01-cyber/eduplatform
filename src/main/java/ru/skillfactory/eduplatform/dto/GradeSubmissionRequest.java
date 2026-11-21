package ru.skillfactory.eduplatform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record GradeSubmissionRequest(
        @NotNull(message = "Необходимо указать оценку")
        @PositiveOrZero(message = "Оценка не может быть отрицательной")
        Integer score,

        @Size(max = 2000, message = "Комментарий не должен превышать 2000 символов")
        String feedback
) {
}