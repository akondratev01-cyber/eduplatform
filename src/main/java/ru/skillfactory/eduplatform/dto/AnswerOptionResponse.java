package ru.skillfactory.eduplatform.dto;

import ru.skillfactory.eduplatform.entity.AnswerOption;

public record AnswerOptionResponse(
        Long id,
        Long questionId,
        String text,
        boolean correct
) {
    public static AnswerOptionResponse from(AnswerOption option) {
        return new AnswerOptionResponse(
                option.getId(),
                option.getQuestion().getId(),
                option.getText(),
                option.isCorrect()
        );
    }
}