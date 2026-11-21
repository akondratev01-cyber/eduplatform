package ru.skillfactory.eduplatform.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.skillfactory.eduplatform.dto.CourseResponse;
import ru.skillfactory.eduplatform.dto.CreateCourseRequest;
import ru.skillfactory.eduplatform.dto.CreateModuleRequest;
import ru.skillfactory.eduplatform.dto.ModuleResponse;
import ru.skillfactory.eduplatform.service.CourseService;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // Создание нового курса
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse createCourse(@Valid @RequestBody CreateCourseRequest request) {
        return CourseResponse.from(
                courseService.createCourse(request.title(), request.description(), request.teacherId(),
                        request.categoryId(), request.duration(), request.startDate()));
    }


    // Получение всех курсов
    @GetMapping
    public List<CourseResponse> getAllCourses() {
        return CourseResponse.from(courseService.getAll());
    }

    // Получение курса по ID
    @GetMapping("/{id}")
    public CourseResponse getCourseById(@PathVariable Long id) {
        return CourseResponse.from(courseService.getById(id));
    }

    // Добавление модуля к курсу
    @PostMapping("/{id}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    public ModuleResponse createModule(@PathVariable Long id, @Valid @RequestBody CreateModuleRequest request) {
        return ModuleResponse.from(
                courseService.addModule(id, request.title(), request.orderIndex(), request.description()));
    }

    // Получение модулей курса
    @GetMapping("/{id}/modules")
    public List<ModuleResponse> getCourseModules(@PathVariable Long id) {
        return courseService.getModules(id).stream().map(ModuleResponse::from).toList();
    }
}
