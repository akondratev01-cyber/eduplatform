package ru.skillfactory.eduplatform.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.skillfactory.eduplatform.entity.Course;
import ru.skillfactory.eduplatform.entity.Enrollment;
import ru.skillfactory.eduplatform.entity.User;
import ru.skillfactory.eduplatform.entity.enums.UserRole;
import ru.skillfactory.eduplatform.repository.CourseRepository;
import ru.skillfactory.eduplatform.repository.EnrollmentRepository;
import ru.skillfactory.eduplatform.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    @Test
    void enrollShouldSaveActiveEnrollment() {
        User student = new User(
                "Иван", "Петров",
                "ivan@example.com", "hash",
                UserRole.STUDENT
        );
        Course course = mock(Course.class);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(student));
        when(courseRepository.findById(1L))
                .thenReturn(Optional.of(course));
        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Enrollment result = enrollmentService.enroll(1L, 1L);

        assertSame(student, result.getUser());
        assertSame(course, result.getCourse());
        assertEquals("ACTIVE", result.getStatus());

        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void enrollShouldRejectNonStudent() {
        User teacher = new User(
                "Алексей", "Смирнов",
                "teacher@example.com", "hash",
                UserRole.TEACHER
        );

        when(userRepository.findById(3L))
                .thenReturn(Optional.of(teacher));

        assertThrows(IllegalArgumentException.class,
                () -> enrollmentService.enroll(3L, 1L));

        verifyNoInteractions(courseRepository, enrollmentRepository);
    }

    @Test
    void enrollShouldRejectDuplicateEnrollment() {
        User student = new User(
                "Иван", "Петров",
                "ivan@example.com", "hash",
                UserRole.STUDENT
        );
        Course course = mock(Course.class);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(student));
        when(courseRepository.findById(1L))
                .thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 1L))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> enrollmentService.enroll(1L, 1L));

        verify(enrollmentRepository, never())
                .save(any(Enrollment.class));
    }

    @Test
    void getStudentEnrollmentsShouldReturnList() {
        Enrollment enrollment = mock(Enrollment.class);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.findByUserId(1L))
                .thenReturn(List.of(enrollment));

        List<Enrollment> result =
                enrollmentService.getStudentEnrollments(1L);

        assertEquals(1, result.size());
        assertSame(enrollment, result.getFirst());
    }

    @Test
    void completeShouldChangeStatusToCompleted() {
        Enrollment enrollment = new Enrollment(
                mock(User.class), mock(Course.class)
        );

        when(enrollmentRepository.findByUserIdAndCourseId(1L, 1L))
                .thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(enrollment))
                .thenReturn(enrollment);

        Enrollment result = enrollmentService.complete(1L, 1L);

        assertEquals("COMPLETED", result.getStatus());
        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void unenrollShouldDeleteExistingEnrollment() {
        Enrollment enrollment = new Enrollment(
                mock(User.class), mock(Course.class)
        );

        when(enrollmentRepository.findByUserIdAndCourseId(1L, 1L))
                .thenReturn(Optional.of(enrollment));

        enrollmentService.unenroll(1L, 1L);

        verify(enrollmentRepository).delete(enrollment);
    }
}