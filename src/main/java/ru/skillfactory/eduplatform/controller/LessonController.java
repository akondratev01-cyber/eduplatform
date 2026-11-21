package ru.skillfactory.eduplatform.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.skillfactory.eduplatform.dto.CreateLessonRequest;
import ru.skillfactory.eduplatform.dto.LessonResponse;
import ru.skillfactory.eduplatform.service.CourseService;

import java.util.List;

@RestController
@RequestMapping("/api/modules/{moduleId}/lessons")
public class LessonController {
    private final CourseService courseService;

    public LessonController(CourseService courseService) {
        this.courseService = courseService;
    }

    // Создание урока в модуле
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LessonResponse createLesson(@PathVariable Long moduleId, @Valid @RequestBody CreateLessonRequest create) {
        return LessonResponse.from(
                courseService.addLesson(moduleId, create.title(), create.content(), create.videoUrl()));
    }

    // Получение всех уроков модуля
    @GetMapping
    public List<LessonResponse> getLessons(@PathVariable Long moduleId) {
        return courseService.getLessons(moduleId).stream().map(LessonResponse::from).toList();
    }
}
