package ru.skillfactory.eduplatform.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.skillfactory.eduplatform.dto.*;
import ru.skillfactory.eduplatform.service.AssignmentService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    // Создание домашнего задания
    @PostMapping("/lessons/{lessonId}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentResponse createAssignment(
            @PathVariable Long lessonId,
            @Valid @RequestBody CreateAssignmentRequest request
    ) {
        return AssignmentResponse.from(assignmentService.createAssignment(
                lessonId,
                request.title(),
                request.description(),
                request.dueDate(),
                request.maxScore()
        ));
    }

    // Получение всех заданий урока
    @GetMapping("/lessons/{lessonId}/assignments")
    public List<AssignmentResponse> getAssignments(@PathVariable Long lessonId) {
        return assignmentService.getByLesson(lessonId)
                .stream()
                .map(AssignmentResponse::from)
                .toList();
    }

    // Отправка решения домашнего задания
    @PostMapping("/assignments/{assignmentId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse submitAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody CreateSubmissionRequest request
    ) {
        return SubmissionResponse.from(assignmentService.submit(
                assignmentId,
                request.studentId(),
                request.content()
        ));
    }

    // Оценивание решения домашнего задания
    @PatchMapping("/submissions/{submissionId}/grade")
    public SubmissionResponse gradeSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody GradeSubmissionRequest request
    ) {
        return SubmissionResponse.from(assignmentService.gradeSubmission(
                submissionId,
                request.score(),
                request.feedback()
        ));
    }


    // Все решения определённого студента
    @GetMapping("/students/{studentId}/submissions")
    public List<SubmissionResponse> getStudentSubmissions(
            @PathVariable Long studentId
    ) {
        return assignmentService.getStudentSubmissions(studentId)
                .stream()
                .map(SubmissionResponse::from)
                .toList();
    }

    // Все решения определённого задания
    @GetMapping("/assignments/{assignmentId}/submissions")
    public List<SubmissionResponse> getAssignmentSubmissions(
            @PathVariable Long assignmentId
    ) {
        return assignmentService.getAssignmentSubmissions(assignmentId)
                .stream()
                .map(SubmissionResponse::from)
                .toList();
    }
}