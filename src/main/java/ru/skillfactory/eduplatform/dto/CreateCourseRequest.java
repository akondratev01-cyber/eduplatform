package ru.skillfactory.eduplatform.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CreateCourseRequest(
        @NotBlank(message = "Название курса обязательно")
        @Size(max = 200, message = "Название курса не может превышать 200 символов")
        String title,
        String description,
        @NotNull(message = "Необходимо указать преподователя")
        @Positive(message = "ID преподавателя должен быть положительным числом")
        Long teacherId,
        @NotNull(message = "Необходимо указать категорию")
        @Positive(message = "ID категории должен быть положительным числом")
        Long categoryId,
        @Positive(message = "Продолжительность курса должна быть положительным числом")
        int duration,
        @NotNull(message = "Необходимо указать дату начала курса")
        @FutureOrPresent(message = "Дата начала курса не может быть в прошлом")
        LocalDate startDate
) {
}