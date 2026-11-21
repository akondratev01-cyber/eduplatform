package ru.skillfactory.eduplatform.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.skillfactory.eduplatform.entity.*;
import ru.skillfactory.eduplatform.entity.Module;
import ru.skillfactory.eduplatform.entity.enums.UserRole;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    private User createStudent() {
        return new User("Иван", "Петров", "test.student@example.com", "encoded-password", UserRole.STUDENT);
    }

    @Test
    void shouldSaveAndFindUser() {
        User saved = userRepository.saveAndFlush(createStudent());

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());

        User found = userRepository.findById(saved.getId()).orElseThrow();

        assertEquals("Иван Петров", found.getName());
        assertEquals("test.student@example.com", found.getEmail());
    }

    @Test
    void shouldFindUserByEmail() {
        userRepository.saveAndFlush(createStudent());

        User found = userRepository.findByEmail("test.student@example.com").orElseThrow();

        assertEquals(UserRole.STUDENT, found.getRole());
        assertTrue(userRepository.existsByEmail("test.student@example.com"));
    }

    @Test
    void shouldUpdateUser() {
        User saved = userRepository.saveAndFlush(createStudent());

        saved.setName("Иван Сидоров");
        userRepository.saveAndFlush(saved);

        User updated = userRepository.findById(saved.getId())
                .orElseThrow();

        assertEquals("Иван Сидоров", updated.getName());
    }

    @Test
    void shouldDeleteUser() {
        User saved = userRepository.saveAndFlush(createStudent());
        Long userId = saved.getId();

        userRepository.delete(saved);
        userRepository.flush();

        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void shouldSaveCourseWithTeacherAndCategory() {
        User teacher = userRepository.saveAndFlush(
                new User("Алексей", "Смирнов", "test.teacher@example.com", "encoded-password", UserRole.TEACHER));

        Category category = categoryRepository.saveAndFlush(new Category("Программирование"));

        Course saved = courseRepository.saveAndFlush(
                new Course("Основы Java", "Введение в программирование", teacher, category, 40,
                        LocalDate.of(2026, 12, 1)));

        Course found = courseRepository.findById(saved.getId()).orElseThrow();

        assertEquals("Основы Java", found.getTitle());
        assertEquals(teacher.getId(), found.getTeacher().getId());
        assertEquals(category.getId(), found.getCategory().getId());
    }

    @Test
    void shouldPreventDeletingCourseWithModules() {
        User teacher = userRepository.saveAndFlush(
                new User("Алексей", "Смирнов", "delete.teacher@example.com", "encoded-password", UserRole.TEACHER));

        Category category = categoryRepository.saveAndFlush(new Category("Тестовая категория"));

        Course course = courseRepository.saveAndFlush(
                new Course("Курс для проверки удаления", "Проверяем ограничения связей", teacher, category, 20,
                        LocalDate.of(2026, 12, 1)));

        moduleRepository.saveAndFlush(
                new Module(course, "Тестовый модуль", 0, "Модуль для проверки удаления"));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update("DELETE FROM courses WHERE id = ?", course.getId()));
    }

    @Test
    void shouldPerformEnrollmentCrud() {
        // Подготавливаем преподавателя и студента
        User teacher = userRepository.saveAndFlush(
                new User("Алексей", "Смирнов", "crud.teacher@example.com", "encoded-password", UserRole.TEACHER));

        User student = userRepository.saveAndFlush(
                new User("Иван", "Петров", "crud.student@example.com", "encoded-password", UserRole.STUDENT));

        Category category = categoryRepository.saveAndFlush(
                new Category("CRUD тестирование")
        );

        Course course = courseRepository.saveAndFlush(
                new Course("Курс для CRUD", "Проверка записей студентов", teacher, category, 20,
                        LocalDate.of(2026, 12, 1)
                )
        );

        // CREATE — создаём запись
        Enrollment enrollment = enrollmentRepository.saveAndFlush(new Enrollment(student, course));

        Long enrollmentId = enrollment.getId();

        assertNotNull(enrollmentId);
        assertEquals("ACTIVE", enrollment.getStatus());

        // READ — получаем запись из репозитория
        Enrollment found = enrollmentRepository.findById(enrollmentId).orElseThrow();

        assertEquals(student.getId(), found.getUser().getId());
        assertEquals(course.getId(), found.getCourse().getId());

        // UPDATE — завершаем обучение
        found.setStatus("COMPLETED");
        enrollmentRepository.saveAndFlush(found);

        Enrollment updated = enrollmentRepository.findById(enrollmentId).orElseThrow();

        assertEquals("COMPLETED", updated.getStatus());

        // DELETE — удаляем запись
        enrollmentRepository.delete(updated);
        enrollmentRepository.flush();

        assertFalse(enrollmentRepository.existsById(enrollmentId));
    }

    @Test
    void shouldPerformSubmissionCrud() {
        User teacher = userRepository.saveAndFlush(
                new User("Алексей", "Смирнов", "submission.teacher@example.com", "encoded-password", UserRole.TEACHER));

        User student = userRepository.saveAndFlush(
                new User("Иван", "Петров", "submission.student@example.com", "encoded-password", UserRole.STUDENT));

        Category category = categoryRepository.saveAndFlush(new Category("Проверка решений"));

        Course course = courseRepository.saveAndFlush(
                new Course("Java CRUD", "Тестирование решений", teacher, category, 20, LocalDate.of(2026, 12, 1)));

        Module module = moduleRepository.saveAndFlush(new Module(course, "Первый модуль", 0, "Описание"));

        Lesson lesson = lessonRepository.saveAndFlush(
                new Lesson(module, "Первый урок", "Материал урока", null));

        Assignment assignment = assignmentRepository.saveAndFlush(
                new Assignment(lesson, "Домашняя работа", "Написать программу", 100));

        // CREATE
        Submission submission = submissionRepository.saveAndFlush(new Submission(assignment, student, "Моё решение"));

        Long submissionId = submission.getId();
        assertNotNull(submissionId);
        assertNotNull(submission.getSubmittedAt());

        // READ
        Submission found = submissionRepository.findById(submissionId)
                .orElseThrow();

        assertEquals("Моё решение", found.getContent());
        assertEquals(student.getId(), found.getStudent().getId());
        assertEquals(assignment.getId(), found.getAssignment().getId());

        // UPDATE
        found.setScore(95);
        found.setFeedback("Отличная работа");
        submissionRepository.saveAndFlush(found);

        Submission updated = submissionRepository.findById(submissionId).orElseThrow();

        assertEquals(95, updated.getScore());
        assertEquals("Отличная работа", updated.getFeedback());

        // DELETE
        submissionRepository.delete(updated);
        submissionRepository.flush();

        assertFalse(submissionRepository.existsById(submissionId));
    }
}