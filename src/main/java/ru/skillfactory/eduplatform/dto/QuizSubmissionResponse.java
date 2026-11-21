package ru.skillfactory.eduplatform.dto;

import ru.skillfactory.eduplatform.entity.QuizSubmission;

import java.time.LocalDateTime;

public record QuizSubmissionResponse(
        Long id,
        Long quizId,
        Long studentId,
        int score,
        LocalDateTime takenAt
) {
    public static QuizSubmissionResponse from(QuizSubmission submission) {
        return new QuizSubmissionResponse(
                submission.getId(),
                submission.getQuiz().getId(),
                submission.getStudent().getId(),
                submission.getScore(),
                submission.getTakenAt()
        );
    }
}