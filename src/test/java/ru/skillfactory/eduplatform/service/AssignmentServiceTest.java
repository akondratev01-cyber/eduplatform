package ru.skillfactory.eduplatform.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.skillfactory.eduplatform.entity.*;
import ru.skillfactory.eduplatform.entity.Module;
import ru.skillfactory.eduplatform.entity.enums.UserRole;
import ru.skillfactory.eduplatform.repository.*;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    @Test
    void createAssignmentShouldSaveValidAssignment() {
        Lesson lesson = mock(Lesson.class);
        LocalDateTime dueDate =
                LocalDateTime.of(2026, 12, 15, 23, 59);

        when(lessonRepository.findById(1L))
                .thenReturn(Optional.of(lesson));
        when(assignmentRepository.save(any(Assignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Assignment result = assignmentService.createAssignment(
                1L,
                " Первая программа на Java ",
                "Написать Hello World",
                dueDate,
                100
        );

        assertEquals("Первая программа на Java", result.getTitle());
        assertEquals(100, result.getMaxScore());
        assertEquals(dueDate, result.getDueDate());
        assertSame(lesson, result.getLesson());

        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    void createAssignmentShouldRejectBlankTitle() {
        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.createAssignment(
                        1L, "   ", "Описание", null, 100
                ));

        verifyNoInteractions(lessonRepository, assignmentRepository);
    }

    @Test
    void createAssignmentShouldRejectInvalidMaxScore() {
        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.createAssignment(
                        1L, "Задание", "Описание", null, 0
                ));

        verifyNoInteractions(lessonRepository, assignmentRepository);
    }

    @Test
    void getByIdShouldRejectMissingAssignment() {
        when(assignmentRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> assignmentService.getById(999L));
    }

    @Test
    void submitShouldSaveStudentSolution() {
        User student = new User(
                "Иван", "Петров",
                "ivan@example.com", "hash",
                UserRole.STUDENT
        );

        Course course = mock(Course.class);
        Module module = mock(Module.class);
        Lesson lesson = mock(Lesson.class);
        Enrollment enrollment = mock(Enrollment.class);

        Assignment assignment =
                new Assignment(lesson, "Задание", "Описание", 100);

        when(assignmentRepository.findById(1L))
                .thenReturn(Optional.of(assignment));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(student));

        when(lesson.getModule()).thenReturn(module);
        when(module.getCourse()).thenReturn(course);
        when(course.getId()).thenReturn(1L);

        when(enrollmentRepository.findByUserIdAndCourseId(1L, 1L))
                .thenReturn(Optional.of(enrollment));
        when(enrollment.getStatus()).thenReturn("ACTIVE");

        when(submissionRepository.save(any(Submission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Submission result = assignmentService.submit(
                1L, 1L, " System.out.println(\"Hello\"); "
        );

        assertSame(assignment, result.getAssignment());
        assertSame(student, result.getStudent());
        assertEquals(
                "System.out.println(\"Hello\");",
                result.getContent()
        );

        verify(submissionRepository).save(any(Submission.class));
    }

    @Test
    void submitShouldRejectBlankContent() {
        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.submit(1L, 1L, "   "));

        verifyNoInteractions(assignmentRepository, submissionRepository);
    }

    @Test
    void gradeSubmissionShouldSaveValidScore() {
        Assignment assignment =
                new Assignment(mock(Lesson.class), "Задание", null, 100);

        Submission submission = new Submission(
                assignment,
                mock(User.class),
                "Решение"
        );

        when(submissionRepository.findById(1L))
                .thenReturn(Optional.of(submission));
        when(submissionRepository.save(submission))
                .thenReturn(submission);

        Submission result = assignmentService.gradeSubmission(
                1L, 95, "Отличная работа"
        );

        assertEquals(95, result.getScore());
        assertEquals("Отличная работа", result.getFeedback());

        verify(submissionRepository).save(submission);
    }

    @Test
    void gradeSubmissionShouldRejectScoreAboveMaximum() {
        Assignment assignment =
                new Assignment(mock(Lesson.class), "Задание", null, 100);

        Submission submission = new Submission(
                assignment,
                mock(User.class),
                "Решение"
        );

        when(submissionRepository.findById(1L))
                .thenReturn(Optional.of(submission));

        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.gradeSubmission(
                        1L, 101, "Комментарий"
                ));

        verify(submissionRepository, never())
                .save(any(Submission.class));
    }
}