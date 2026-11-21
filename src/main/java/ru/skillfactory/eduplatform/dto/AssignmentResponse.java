package ru.skillfactory.eduplatform.dto;

import ru.skillfactory.eduplatform.entity.Assignment;

import java.time.LocalDateTime;

public record AssignmentResponse(
        Long id,
        Long lessonId,
        String title,
        String description,
        LocalDateTime dueDate,
        int maxScore
) {
    public static AssignmentResponse from(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getLesson().getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDueDate(),
                assignment.getMaxScore()
        );
    }
}