package ru.skillfactory.eduplatform.dto;

import ru.skillfactory.eduplatform.entity.Quiz;

public record QuizResponse(
        Long id,
        Long moduleId,
        String title,
        int timeLimit
) {
    public static QuizResponse from(Quiz quiz) {
        return new QuizResponse(
                quiz.getId(),
                quiz.getModule().getId(),
                quiz.getTitle(),
                quiz.getTimeLimit()
        );
    }
}