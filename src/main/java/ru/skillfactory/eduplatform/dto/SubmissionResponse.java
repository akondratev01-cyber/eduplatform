package ru.skillfactory.eduplatform.dto;

import ru.skillfactory.eduplatform.entity.Submission;

import java.time.LocalDateTime;

public record SubmissionResponse(
        Long id,
        Long assignmentId,
        Long studentId,
        LocalDateTime submittedAt,
        String content,
        Integer score,
        String feedback
) {
    public static SubmissionResponse from(Submission submission) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getAssignment().getId(),
                submission.getStudent().getId(),
                submission.getSubmittedAt(),
                submission.getContent(),
                submission.getScore(),
                submission.getFeedback()
        );
    }
}
