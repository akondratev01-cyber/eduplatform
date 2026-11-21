package ru.skillfactory.eduplatform.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.skillfactory.eduplatform.entity.Category;
import ru.skillfactory.eduplatform.entity.Course;
import ru.skillfactory.eduplatform.entity.Module;
import ru.skillfactory.eduplatform.entity.User;
import ru.skillfactory.eduplatform.entity.enums.UserRole;
import ru.skillfactory.eduplatform.repository.*;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    private final LocalDate startDate = LocalDate.of(2026, 12, 1);
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ModuleRepository moduleRepository;
    @Mock
    private LessonRepository lessonRepository;
    @InjectMocks
    private CourseService courseService;

    @Test
    void createCourseShouldSaveValidCourse() {
        User teacher = new User(
                "Алексей", "Смирнов",
                "teacher@example.com", "hash",
                UserRole.TEACHER
        );
        Category category = new Category("Программирование");

        when(userRepository.findById(3L))
                .thenReturn(Optional.of(teacher));
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));
        when(courseRepository.save(any(Course.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Course result = courseService.createCourse(
                " Основы Java ",
                "Введение в программирование",
                3L, 1L, 40, startDate
        );

        assertEquals("Основы Java", result.getTitle());
        assertEquals(40, result.getDuration());
        assertEquals(startDate, result.getStartDate());
        assertSame(teacher, result.getTeacher());
        assertSame(category, result.getCategory());

        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void createCourseShouldRejectBlankTitle() {
        assertThrows(IllegalArgumentException.class,
                () -> courseService.createCourse(
                        "   ", "Описание",
                        3L, 1L, 40, startDate
                ));

        verifyNoInteractions(courseRepository, userRepository,
                categoryRepository);
    }

    @Test
    void createCourseShouldRejectStudentAsTeacher() {
        User student = new User(
                "Иван", "Петров",
                "student@example.com", "hash",
                UserRole.STUDENT
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(student));

        assertThrows(IllegalArgumentException.class,
                () -> courseService.createCourse(
                        "Основы Java", "Описание",
                        1L, 1L, 40, startDate
                ));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void createCourseShouldRejectMissingCategory() {
        User teacher = new User(
                "Алексей", "Смирнов",
                "teacher@example.com", "hash",
                UserRole.TEACHER
        );

        when(userRepository.findById(3L))
                .thenReturn(Optional.of(teacher));
        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> courseService.createCourse(
                        "Основы Java", "Описание",
                        3L, 999L, 40, startDate
                ));

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void getByIdShouldRejectMissingCourse() {
        when(courseRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> courseService.getById(999L));

        verify(courseRepository).findById(999L);
    }

    @Test
    void addModuleShouldSaveAndAttachModule() {
        Course course = mock(Course.class);
        java.util.List<Module> modules = new java.util.ArrayList<>();

        when(courseRepository.findById(1L))
                .thenReturn(Optional.of(course));
        when(course.getModules()).thenReturn(modules);
        when(moduleRepository.save(any(Module.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Module result = courseService.addModule(
                1L, " Введение в Java ", 0,
                "Первый модуль курса"
        );

        assertEquals("Введение в Java", result.getTitle());
        assertEquals(0, result.getOrderIndex());
        assertSame(course, result.getCourse());
        assertTrue(modules.contains(result));

        verify(moduleRepository).save(any(Module.class));
    }

    @Test
    void deleteCourseShouldDeleteExistingCourse() {
        Course course = mock(Course.class);

        when(courseRepository.findById(1L))
                .thenReturn(Optional.of(course));

        courseService.deleteCourse(1L);

        verify(courseRepository).delete(course);
    }
}