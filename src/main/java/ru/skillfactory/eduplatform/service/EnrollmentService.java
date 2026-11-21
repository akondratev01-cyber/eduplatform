package ru.skillfactory.eduplatform.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillfactory.eduplatform.entity.Course;
import ru.skillfactory.eduplatform.entity.Enrollment;
import ru.skillfactory.eduplatform.entity.User;
import ru.skillfactory.eduplatform.entity.enums.UserRole;
import ru.skillfactory.eduplatform.repository.CourseRepository;
import ru.skillfactory.eduplatform.repository.EnrollmentRepository;
import ru.skillfactory.eduplatform.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, UserRepository userRepository, CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    // Записать студента на курс
    public Enrollment enroll(Long userId, Long courseId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));

        if (user.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("Записаться на курс может только студент");
        }

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NoSuchElementException("Курс с ID " + courseId + " не найден"));

        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new IllegalArgumentException("Студент уже записан на этот курс");
        }

        Enrollment enrollment = new Enrollment(user, course);

        return enrollmentRepository.save(enrollment);
    }

    // Получить все записи студента
    @Transactional(readOnly = true)
    public List<Enrollment> getStudentEnrollments(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("Пользователь не найден");
        }

        return enrollmentRepository.findByUserId(userId);
    }

    // Получить всех записанных на курс
    @Transactional(readOnly = true)
    public List<Enrollment> getCourseEnrollments(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new NoSuchElementException("Курс не найден");
        }

        return enrollmentRepository.findByCourseId(courseId);
    }

    // Получить запись конкретного студента на курс
    @Transactional(readOnly = true)
    public Enrollment getEnrollment(Long userId, Long courseId) {
        return enrollmentRepository.findByUserIdAndCourseId(userId, courseId).orElseThrow(() -> new NoSuchElementException("Запись студента на курс не найдена"));
    }

    // Завершить обучение
    public Enrollment complete(Long userId, Long courseId) {
        Enrollment enrollment = getEnrollment(userId, courseId);
        enrollment.setStatus("COMPLETED");
        return enrollmentRepository.save(enrollment);
    }

    // Отписать студента от курса
    public void unenroll(Long userId, Long courseId) {
        Enrollment enrollment = getEnrollment(userId, courseId);
        enrollmentRepository.delete(enrollment);
    }
}