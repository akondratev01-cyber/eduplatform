package ru.skillfactory.eduplatform.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.skillfactory.eduplatform.entity.Category;
import ru.skillfactory.eduplatform.entity.Course;
import ru.skillfactory.eduplatform.entity.Module;
import ru.skillfactory.eduplatform.entity.User;
import ru.skillfactory.eduplatform.entity.enums.UserRole;
import ru.skillfactory.eduplatform.service.UserService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
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

    private User createStudent() {
        return new User(
                "Иван", "Петров",
                "test.student@example.com",
                "encoded-password",
                UserRole.STUDENT
        );
    }

    @Test
    void shouldSaveAndFindUser() {
        User saved = userRepository.saveAndFlush(createStudent());

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());

        User found = userRepository.findById(saved.getId())
                .orElseThrow();

        assertEquals("Иван Петров", found.getName());
        assertEquals("test.student@example.com", found.getEmail());
    }

    @Test
    void shouldFindUserByEmail() {
        userRepository.saveAndFlush(createStudent());

        User found = userRepository
                .findByEmail("test.student@example.com")
                .orElseThrow();

        assertEquals(UserRole.STUDENT, found.getRole());
        assertTrue(userRepository.existsByEmail(
                "test.student@example.com"
        ));
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
                new User(
                        "Алексей", "Смирнов",
                        "test.teacher@example.com",
                        "encoded-password",
                        UserRole.TEACHER
                )
        );

        Category category = categoryRepository.saveAndFlush(
                new Category("Программирование")
        );

        Course saved = courseRepository.saveAndFlush(
                new Course(
                        "Основы Java",
                        "Введение в программирование",
                        teacher,
                        category,
                        40,
                        LocalDate.of(2026, 12, 1)
                )
        );

        Course found = courseRepository.findById(saved.getId())
                .orElseThrow();

        assertEquals("Основы Java", found.getTitle());
        assertEquals(teacher.getId(), found.getTeacher().getId());
        assertEquals(category.getId(), found.getCategory().getId());
    }

    @Test
    void shouldPreventDeletingCourseWithModules() {
        User teacher = userRepository.saveAndFlush(
                new User(
                        "Алексей", "Смирнов",
                        "delete.teacher@example.com",
                        "encoded-password",
                        UserRole.TEACHER
                )
        );

        Category category = categoryRepository.saveAndFlush(
                new Category("Тестовая категория")
        );

        Course course = courseRepository.saveAndFlush(
                new Course(
                        "Курс для проверки удаления",
                        "Проверяем ограничения связей",
                        teacher,
                        category,
                        20,
                        LocalDate.of(2026, 12, 1)
                )
        );

        moduleRepository.saveAndFlush(
                new Module(
                        course,
                        "Тестовый модуль",
                        0,
                        "Модуль для проверки удаления"
                )
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        "DELETE FROM courses WHERE id = ?",
                        course.getId()
                )
        );
    }
}