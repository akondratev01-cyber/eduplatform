package ru.skillfactory.eduplatform.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillfactory.eduplatform.entity.*;
import ru.skillfactory.eduplatform.entity.enums.UserRole;
import ru.skillfactory.eduplatform.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    public AssignmentService(AssignmentRepository assignmentRepository, SubmissionRepository submissionRepository, LessonRepository lessonRepository, UserRepository userRepository, EnrollmentRepository enrollmentRepository) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    // Создание домашнего задания
    public Assignment createAssignment(Long lessonId, String title, String description, LocalDateTime dueDate, int maxScore) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Название задания обязательно");
        }

        if (maxScore <= 0) {
            throw new IllegalArgumentException("Максимальный балл должен быть положительным");
        }

        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new NoSuchElementException("Урок с ID " + lessonId + " не найден"));

        Assignment assignment = new Assignment(lesson, title.trim(), description, maxScore);

        assignment.setDueDate(dueDate);

        return assignmentRepository.save(assignment);
    }

    // Получение задания
    @Transactional(readOnly = true)
    public Assignment getById(Long id) {
        return assignmentRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Задание с ID " + id + " не найдено"));
    }

    // Задания определённого урока
    @Transactional(readOnly = true)
    public List<Assignment> getByLesson(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new NoSuchElementException("Урок не найден");
        }

        return assignmentRepository.findByLessonId(lessonId);
    }

    // Отправка решения студентом
    public Submission submit(Long assignmentId, Long studentId, String content) {

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Решение не может быть пустым");
        }

        Assignment assignment = getById(assignmentId);

        User student = userRepository.findById(studentId).orElseThrow(() -> new NoSuchElementException("Студент не найден"));

        if (student.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("Отправлять решения может только студент");
        }

        Long courseId = assignment.getLesson().getModule().getCourse().getId();

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(studentId, courseId).orElseThrow(() -> new IllegalArgumentException("Студент не записан на этот курс"));

        if (!"ACTIVE".equals(enrollment.getStatus())) {
            throw new IllegalArgumentException("Запись студента на курс неактивна");
        }

        if (assignment.getDueDate() != null && LocalDateTime.now().isAfter(assignment.getDueDate())) {
            throw new IllegalArgumentException("Срок сдачи задания истёк");
        }

        if (submissionRepository.existsByStudentIdAndAssignmentId(studentId, assignmentId)) {
            throw new IllegalArgumentException("Решение этого задания уже отправлено");
        }

        Submission submission = new Submission(assignment, student, content.trim());

        return submissionRepository.save(submission);
    }

    // Оценивание решения
    public Submission gradeSubmission(Long submissionId, int score, String feedback) {

        Submission submission = submissionRepository.findById(submissionId).orElseThrow(() -> new NoSuchElementException("Решение не найдено"));

        int maxScore = submission.getAssignment().getMaxScore();

        if (score < 0 || score > maxScore) {
            throw new IllegalArgumentException("Оценка должна быть от 0 до " + maxScore);
        }

        if (feedback != null && feedback.length() > 2000) {
            throw new IllegalArgumentException("Комментарий не должен превышать 2000 символов");
        }

        submission.setScore(score);
        submission.setFeedback(feedback);

        return submissionRepository.save(submission);
    }

    // Решения определённого студента
    @Transactional(readOnly = true)
    public List<Submission> getStudentSubmissions(Long studentId) {
        if (!userRepository.existsById(studentId)) {
            throw new NoSuchElementException("Студент не найден");
        }

        return submissionRepository.findByStudentId(studentId);
    }

    // Все решения определённого задания
    @Transactional(readOnly = true)
    public List<Submission> getAssignmentSubmissions(Long assignmentId) {
        getById(assignmentId);
        return submissionRepository.findByAssignmentId(assignmentId);
    }
}