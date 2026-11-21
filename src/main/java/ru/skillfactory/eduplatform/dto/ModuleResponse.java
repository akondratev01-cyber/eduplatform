package ru.skillfactory.eduplatform.dto;

import ru.skillfactory.eduplatform.entity.Module;

public record ModuleResponse(
        Long id,
        Long courseId,
        String title,
        int orderIndex,
        String description
) {
    public static ModuleResponse from(Module module) {
        return new ModuleResponse(
                module.getId(),
                module.getCourse().getId(),
                module.getTitle(),
                module.getOrderIndex(),
                module.getDescription());
    }
}