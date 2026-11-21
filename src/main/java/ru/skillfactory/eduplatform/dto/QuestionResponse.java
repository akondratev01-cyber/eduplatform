package ru.skillfactory.eduplatform.dto;

import ru.skillfactory.eduplatform.entity.Question;

public record QuestionResponse(
        Long id,
        Long quizId,
        String text,
        String type
) {
    public static QuestionResponse from(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getQuiz().getId(),
                question.getText(),
                question.getType()
        );
    }
}