package ru.skillfactory.eduplatform.dto;

import ru.skillfactory.eduplatform.entity.Lesson;

public record LessonResponse(
        Long id,
        Long moduleId,
        String title,
        String content,
        String videoUrl
) {
    public static LessonResponse from(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getModule().getId(),
                lesson.getTitle(),
                lesson.getContent(),
                lesson.getVideoUrl()
        );
    }
}
