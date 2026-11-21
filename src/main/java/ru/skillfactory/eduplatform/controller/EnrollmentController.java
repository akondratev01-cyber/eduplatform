package ru.skillfactory.eduplatform.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.skillfactory.eduplatform.dto.CreateEnrollmentRequest;
import ru.skillfactory.eduplatform.dto.EnrollmentResponse;
import ru.skillfactory.eduplatform.service.EnrollmentService;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    // Запись студента на курс
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse enroll(
            @Valid @RequestBody CreateEnrollmentRequest request
    ) {
        return EnrollmentResponse.from(enrollmentService.enroll(
                request.studentId(),
                request.courseId()
        ));
    }

    // Получение всех записей студента
    @GetMapping("/students/{userId}")
    public List<EnrollmentResponse> getStudentEnrollments(
            @PathVariable Long userId
    ) {
        return enrollmentService.getStudentEnrollments(userId)
                .stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    // Получение студентов, записанных на курс
    @GetMapping("/courses/{courseId}")
    public List<EnrollmentResponse> getCourseEnrollments(
            @PathVariable Long courseId
    ) {
        return enrollmentService.getCourseEnrollments(courseId)
                .stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    // Отписать студента от курса
    @DeleteMapping("/students/{userId}/courses/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unenroll(
            @PathVariable Long userId,
            @PathVariable Long courseId
    ) {
        enrollmentService.unenroll(userId, courseId);
    }
}
