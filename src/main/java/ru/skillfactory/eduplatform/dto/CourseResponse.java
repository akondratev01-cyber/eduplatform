package ru.skillfactory.eduplatform.dto;

import ru.skillfactory.eduplatform.entity.Course;

import java.time.LocalDate;
import java.util.List;

public record CourseResponse(Long id, String title, String description, Long teacherId, Long categoryId, int duration, LocalDate startDate) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getTeacher().getId(),
                course.getCategory().getId(),
                course.getDuration(),
                course.getStartDate()
        );
    }

    public static List<CourseResponse> from(List<Course> courses) {
        return courses.stream()
                .map(CourseResponse::from)
                .toList();
    }
}
