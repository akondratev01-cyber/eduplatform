package ru.skillfactory.eduplatform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateModuleRequest(
        @NotBlank(message = "Название модуля обязательно")
        @Size(max = 255, message = "Название модуля не может превышать 255 символов")
        String title,
        @NotNull(message = "Необходимо указать порядковый номер")
        @Min(value = 0, message = "Порядковый номер должен быть положительным числом")
        Integer orderIndex,
        String description
) {
}
