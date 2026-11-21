package ru.skillfactory.eduplatform.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillfactory.eduplatform.entity.*;
import ru.skillfactory.eduplatform.entity.Module;
import ru.skillfactory.eduplatform.entity.enums.UserRole;
import ru.skillfactory.eduplatform.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseReviewRepository courseReviewRepository;

    public CourseService(CourseRepository courseRepository, CategoryRepository categoryRepository,
            UserRepository userRepository, ModuleRepository moduleRepository, LessonRepository lessonRepository,
            EnrollmentRepository enrollmentRepository, CourseReviewRepository courseReviewRepository) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseReviewRepository = courseReviewRepository;
    }

    // Создание курса
    public Course createCourse(String title, String description, Long teacherId, Long categoryId, int duration,
            LocalDate startDate) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Название курса обязательно");
        }

        if (duration <= 0) {
            throw new IllegalArgumentException("Продолжительность курса должна быть положительной");
        }

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new NoSuchElementException("Преподаватель с ID " + teacherId + " не найден"));

        if (teacher.getRole() != UserRole.TEACHER) {
            throw new IllegalArgumentException("Пользователь не является преподавателем");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("Категория с ID " + categoryId + " не найдена"));

        Course course = new Course(title.trim(), description, teacher, category, duration, startDate);

        return courseRepository.save(course);
    }

    // Получение курса по ID
    @Transactional(readOnly = true)
    public Course getById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Курс с ID " + id + " не найден"));
    }

    // Получение всех курсов
    @Transactional(readOnly = true)
    public List<Course> getAll() {
        return courseRepository.findAll();
    }

    // Курсы преподавателя
    @Transactional(readOnly = true)
    public List<Course> getByTeacher(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    // Курсы определённой категории
    @Transactional(readOnly = true)
    public List<Course> getByCategory(Long categoryId) {
        return courseRepository.findByCategoryId(categoryId);
    }

    // Получение курса вместе с модулями
    @Transactional(readOnly = true)
    public Course getWithModules(Long id) {
        return courseRepository.findWithModulesById(id)
                .orElseThrow(() -> new NoSuchElementException("Курс с ID " + id + " не найден"));
    }

    // Добавление модуля к курсу
    public Module addModule(Long courseId,
            String title,
            int orderIndex,
            String description) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Название модуля обязательно");
        }

        if (orderIndex < 0) {
            throw new IllegalArgumentException("Порядковый номер модуля не может быть отрицательным");
        }

        Course course = getById(courseId);

        Module module = new Module(course, title.trim(), orderIndex, description);

        Module savedModule = moduleRepository.save(module);
        course.getModules().add(savedModule);

        return savedModule;
    }

    // Добавление урока к модулю
    public Lesson addLesson(Long moduleId, String title, String content, String videoUrl) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Название урока обязательно");
        }

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new NoSuchElementException("Модуль с ID " + moduleId + " не найден"));

        Lesson lesson = new Lesson(module, title.trim(), content, videoUrl);

        Lesson savedLesson = lessonRepository.save(lesson);
        module.getLessons().add(savedLesson);

        return savedLesson;
    }

    // Получение модулей курса
    @Transactional(readOnly = true)
    public List<Module> getModules(Long courseId) {
        getById(courseId);
        return moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
    }

    // Получение уроков модуля
    @Transactional(readOnly = true)
    public List<Lesson> getLessons(Long moduleId) {
        if (!moduleRepository.existsById(moduleId)) {
            throw new NoSuchElementException("Модуль с ID " + moduleId + " не найден");
        }

        return lessonRepository.findByModuleId(moduleId);
    }

    // Удаление курса
    public void deleteCourse(Long id) {
        Course course = getById(id);

        if (!moduleRepository.findByCourseIdOrderByOrderIndexAsc(id).isEmpty()) {
            throw new DataIntegrityViolationException("Нельзя удалить курс: он содержит модули");
        }

        if (enrollmentRepository.existsByCourseId(id)) {
            throw new DataIntegrityViolationException("Нельзя удалить курс: на него записаны студенты");
        }

        if (courseReviewRepository.existsByCourseId(id)) {
            throw new DataIntegrityViolationException("Нельзя удалить курс: у него есть отзывы");
        }

        courseRepository.delete(course);
    }
}