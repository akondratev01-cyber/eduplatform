package ru.skillfactory.eduplatform.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.skillfactory.eduplatform.dto.*;
import ru.skillfactory.eduplatform.service.QuizService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    // Создание теста для модуля
    @PostMapping("/modules/{moduleId}/quizzes")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResponse createQuiz(
            @PathVariable Long moduleId,
            @Valid @RequestBody CreateQuizRequest request
    ) {
        return QuizResponse.from(quizService.createQuiz(
                moduleId,
                request.title(),
                request.timeLimit()
        ));
    }

    // Получение тестов модуля
    @GetMapping("/modules/{moduleId}/quizzes")
    public List<QuizResponse> getQuizzes(@PathVariable Long moduleId) {
        return quizService.getByModule(moduleId)
                .stream()
                .map(QuizResponse::from)
                .toList();
    }

    // Добавление вопроса в тест
    @PostMapping("/quizzes/{quizId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse createQuestion(
            @PathVariable Long quizId,
            @Valid @RequestBody CreateQuestionRequest request
    ) {
        return QuestionResponse.from(quizService.addQuestion(
                quizId,
                request.text(),
                request.type()
        ));
    }

    // Добавление варианта ответа к вопросу
    @PostMapping("/questions/{questionId}/options")
    @ResponseStatus(HttpStatus.CREATED)
    public AnswerOptionResponse createAnswerOption(
            @PathVariable Long questionId,
            @Valid @RequestBody CreateAnswerOptionRequest request
    ) {
        return AnswerOptionResponse.from(quizService.addAnswerOption(
                questionId,
                request.text(),
                request.correct()
        ));
    }

    // Прохождение теста студентом
    @PostMapping("/quizzes/{quizId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizSubmissionResponse takeQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody TakeQuizRequest request
    ) {
        return QuizSubmissionResponse.from(quizService.takeQuiz(
                request.studentId(),
                quizId,
                request.answers()
        ));
    }

    // Результаты всех тестов определённого студента
    @GetMapping("/students/{studentId}/quiz-results")
    public List<QuizSubmissionResponse> getStudentResults(@PathVariable Long studentId) {
        return quizService.getStudentResults(studentId)
                .stream()
                .map(QuizSubmissionResponse::from)
                .toList();
    }

    // Результаты прохождения определённого теста
    @GetMapping("/quizzes/{quizId}/results")
    public List<QuizSubmissionResponse> getQuizResults(@PathVariable Long quizId) {
        return quizService.getQuizResults(quizId)
                .stream()
                .map(QuizSubmissionResponse::from)
                .toList();
    }
}