package ru.skillfactory.eduplatform.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillfactory.eduplatform.entity.*;
import ru.skillfactory.eduplatform.entity.Module;
import ru.skillfactory.eduplatform.entity.enums.UserRole;
import ru.skillfactory.eduplatform.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final QuizSubmissionRepository resultRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    public QuizService(
            QuizRepository quizRepository,
            QuestionRepository questionRepository,
            AnswerOptionRepository answerOptionRepository,
            QuizSubmissionRepository resultRepository,
            ModuleRepository moduleRepository,
            UserRepository userRepository,
            EnrollmentRepository enrollmentRepository) {

        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.answerOptionRepository = answerOptionRepository;
        this.resultRepository = resultRepository;
        this.moduleRepository = moduleRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    // Создание теста для модуля
    public Quiz createQuiz(Long moduleId, String title, int timeLimit) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Название теста обязательно");
        }

        if (timeLimit <= 0) {
            throw new IllegalArgumentException("Лимит времени должен быть положительным");
        }

        Module module = moduleRepository.findById(moduleId).orElseThrow(() -> new NoSuchElementException("Модуль не найден"));

        return quizRepository.save(new Quiz(module, title.trim(), timeLimit));
    }

    // Добавление вопроса
    public Question addQuestion(Long quizId, String text, String type) {

        Quiz quiz = getById(quizId);

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Текст вопроса обязателен");
        }

        if (!"SINGLE_CHOICE".equals(type) && !"MULTIPLE_CHOICE".equals(type)) {
            throw new IllegalArgumentException("Тип вопроса должен быть SINGLE_CHOICE или MULTIPLE_CHOICE");
        }

        Question question = questionRepository.save(new Question(quiz, text.trim(), type));

        quiz.getQuestions().add(question);

        return question;
    }

    // Добавление варианта ответа
    public AnswerOption addAnswerOption(Long questionId, String text, boolean correct) {

        Question question = questionRepository.findById(questionId).orElseThrow(() -> new NoSuchElementException("Вопрос не найден"));

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Текст варианта ответа обязателен");
        }

        if (text.trim().length() > 1000) {
            throw new IllegalArgumentException("Вариант ответа слишком длинный");
        }

        AnswerOption option = answerOptionRepository.save(new AnswerOption(question, text.trim(), correct));

        question.getAnswerOptions().add(option);

        return option;
    }

    // Получение теста
    @Transactional(readOnly = true)
    public Quiz getById(Long id) {
        return quizRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Тест с ID " + id + " не найден"));
    }

    // Тесты модуля
    @Transactional(readOnly = true)
    public List<Quiz> getByModule(Long moduleId) {
        if (!moduleRepository.existsById(moduleId)) {
            throw new NoSuchElementException("Модуль не найден");
        }

        return quizRepository.findByModuleId(moduleId);
    }

    // Прохождение теста и автоматическая оценка
    public QuizSubmission takeQuiz(Long studentId, Long quizId, Map<Long, List<Long>> answers) {

        if (answers == null) {
            throw new IllegalArgumentException("Необходимо передать ответы");
        }

        User student = userRepository.findById(studentId).orElseThrow(() -> new NoSuchElementException("Студент не найден"));

        if (student.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("Проходить тест может только студент");
        }

        Quiz quiz = getById(quizId);

        Long courseId = quiz.getModule().getCourse().getId();

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(studentId, courseId).orElseThrow(() -> new IllegalArgumentException("Студент не записан на курс"));

        if (!"ACTIVE".equals(enrollment.getStatus())) {
            throw new IllegalArgumentException("Запись студента на курс неактивна");
        }

        List<Question> questions = questionRepository.findByQuizId(quizId);

        if (questions.isEmpty()) {
            throw new IllegalArgumentException("В тесте отсутствуют вопросы");
        }

        Set<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toSet());

        if (!answers.keySet().equals(questionIds)) {
            throw new IllegalArgumentException("Необходимо ответить на все вопросы теста");
        }

        int correctCount = 0;

        for (Question question : questions) {
            List<Long> selectedIds = answers.get(question.getId());

            if (selectedIds == null || selectedIds.isEmpty() || selectedIds.contains(null)) {
                throw new IllegalArgumentException("Не выбран ответ на вопрос " + question.getId());
            }

            Set<Long> selected = new HashSet<>(selectedIds);

            if (selected.size() != selectedIds.size()) {
                throw new IllegalArgumentException("Один вариант ответа выбран несколько раз");
            }

            if ("SINGLE_CHOICE".equals(question.getType()) && selected.size() != 1) {
                throw new IllegalArgumentException("Для вопроса с одиночным выбором нужен один ответ");
            }

            List<AnswerOption> options = answerOptionRepository.findByQuestionId(question.getId());

            Set<Long> availableIds = options.stream().map(AnswerOption::getId).collect(Collectors.toSet());

            if (!availableIds.containsAll(selected)) {
                throw new IllegalArgumentException("Выбран вариант ответа из другого вопроса");
            }

            Set<Long> correctIds = options.stream().filter(AnswerOption::isCorrect).map(AnswerOption::getId).collect(Collectors.toSet());

            if (correctIds.isEmpty()) {
                throw new IllegalStateException("Для вопроса не задан правильный ответ");
            }

            if (selected.equals(correctIds)) {
                correctCount++;
            }
        }

        // Итоговая оценка: процент правильных ответов
        int score = (int) Math.round(correctCount * 100.0 / questions.size());

        QuizSubmission result = new QuizSubmission(quiz, student, score);

        return resultRepository.save(result);
    }

    // Результаты конкретного студента
    @Transactional(readOnly = true)
    public List<QuizSubmission> getStudentResults(Long studentId) {
        if (!userRepository.existsById(studentId)) {
            throw new NoSuchElementException("Студент не найден");
        }

        return resultRepository.findByStudentId(studentId);
    }

    // Все результаты конкретного теста
    @Transactional(readOnly = true)
    public List<QuizSubmission> getQuizResults(Long quizId) {
        getById(quizId);
        return resultRepository.findByQuizId(quizId);
    }
}