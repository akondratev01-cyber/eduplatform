package ru.skillfactory.eduplatform.dto;

import ru.skillfactory.eduplatform.entity.Enrollment;

import java.time.LocalDate;

public record EnrollmentResponse(
        Long id,
        Long studentId,
        Long courseId,
        LocalDate enrollmentDate,
        String status
) {
    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getUser().getId(),
                enrollment.getCourse().getId(),
                enrollment.getEnrollDate(),
                enrollment.getStatus()
        );
    }
}
