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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerOptionRepository answerOptionRepository;

    @Mock
    private QuizSubmissionRepository resultRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private QuizService quizService;

    @Test
    void createQuizShouldSaveValidQuiz() {
        Module module = mock(Module.class);

        when(moduleRepository.findById(1L))
                .thenReturn(Optional.of(module));

        when(quizRepository.save(any(Quiz.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Quiz result = quizService.createQuiz(
                1L, " Основы Java ", 30
        );

        assertEquals("Основы Java", result.getTitle());
        assertEquals(30, result.getTimeLimit());
        assertSame(module, result.getModule());

        verify(quizRepository).save(any(Quiz.class));
    }

    @Test
    void createQuizShouldRejectInvalidTimeLimit() {
        assertThrows(IllegalArgumentException.class,
                () -> quizService.createQuiz(
                        1L, "Основы Java", 0
                ));

        verifyNoInteractions(moduleRepository, quizRepository);
    }

    @Test
    void addQuestionShouldSaveSingleChoiceQuestion() {
        Quiz quiz = mock(Quiz.class);
        List<Question> questions = new java.util.ArrayList<>();

        when(quizRepository.findById(1L))
                .thenReturn(Optional.of(quiz));

        when(quiz.getQuestions()).thenReturn(questions);

        when(questionRepository.save(any(Question.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Question result = quizService.addQuestion(
                1L, " Какой метод является точкой входа? ",
                "SINGLE_CHOICE"
        );

        assertEquals(
                "Какой метод является точкой входа?",
                result.getText()
        );
        assertEquals("SINGLE_CHOICE", result.getType());
        assertSame(quiz, result.getQuiz());
        assertTrue(questions.contains(result));
    }

    @Test
    void addAnswerOptionShouldSaveCorrectOption() {
        Question question = mock(Question.class);
        List<AnswerOption> options =
                new java.util.ArrayList<>();

        when(questionRepository.findById(1L))
                .thenReturn(Optional.of(question));

        when(question.getAnswerOptions()).thenReturn(options);

        when(answerOptionRepository.save(any(AnswerOption.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnswerOption result = quizService.addAnswerOption(
                1L, " main() ", true
        );

        assertEquals("main()", result.getText());
        assertTrue(result.isCorrect());
        assertSame(question, result.getQuestion());
        assertTrue(options.contains(result));
    }

    @Test
    void takeQuizShouldCalculate100Points() {
        prepareQuizWithTwoQuestions();

        when(resultRepository.save(any(QuizSubmission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QuizSubmission result = quizService.takeQuiz(
                1L, 1L,
                Map.of(
                        1L, new java.util.ArrayList<>(List.of(1L)),
                        2L, new java.util.ArrayList<>(List.of(4L, 6L))
                )
        );

        assertEquals(100, result.getScore());
        verify(resultRepository).save(any(QuizSubmission.class));
    }

    @Test
    void takeQuizShouldCalculate50Points() {
        prepareQuizWithTwoQuestions();

        when(resultRepository.save(any(QuizSubmission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QuizSubmission result = quizService.takeQuiz(
                1L, 1L,
                Map.of(
                        1L, new java.util.ArrayList<>(List.of(1L)),
                        2L, new java.util.ArrayList<>(List.of(4L))
                )
        );

        assertEquals(50, result.getScore());
        verify(resultRepository).save(any(QuizSubmission.class));
    }

    @Test
    void takeQuizShouldRejectTeacher() {
        User teacher = mock(User.class);

        when(userRepository.findById(3L))
                .thenReturn(Optional.of(teacher));
        when(teacher.getRole()).thenReturn(UserRole.TEACHER);

        assertThrows(IllegalArgumentException.class,
                () -> quizService.takeQuiz(
                        3L, 1L, Map.of(1L, List.of(1L))
                ));

        verifyNoInteractions(resultRepository);
    }

    @Test
    void takeQuizShouldRejectMissingAnswers() {
        prepareStudentEnrollment();

        Question question = mock(Question.class);

        when(questionRepository.findByQuizId(1L))
                .thenReturn(List.of(question));
        when(question.getId()).thenReturn(1L);

        assertThrows(IllegalArgumentException.class,
                () -> quizService.takeQuiz(
                        1L, 1L, Map.of()
                ));

        verify(resultRepository, never())
                .save(any(QuizSubmission.class));
    }

    private void prepareStudentEnrollment() {
        User student = mock(User.class);
        Quiz quiz = mock(Quiz.class);
        Module module = mock(Module.class);
        Course course = mock(Course.class);
        Enrollment enrollment = mock(Enrollment.class);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(student));
        when(student.getRole()).thenReturn(UserRole.STUDENT);

        when(quizRepository.findById(1L))
                .thenReturn(Optional.of(quiz));
        when(quiz.getModule()).thenReturn(module);
        when(module.getCourse()).thenReturn(course);
        when(course.getId()).thenReturn(1L);

        when(enrollmentRepository.findByUserIdAndCourseId(1L, 1L))
                .thenReturn(Optional.of(enrollment));
        when(enrollment.getStatus()).thenReturn("ACTIVE");
    }

    private void prepareQuizWithTwoQuestions() {
        prepareStudentEnrollment();

        Question first = mock(Question.class);
        Question second = mock(Question.class);

        when(first.getId()).thenReturn(1L);
        when(second.getId()).thenReturn(2L);
        when(first.getType()).thenReturn("SINGLE_CHOICE");
        when(second.getType()).thenReturn("MULTIPLE_CHOICE");

        when(questionRepository.findByQuizId(1L))
                .thenReturn(List.of(first, second));

        AnswerOption main = option(1L, true);
        AnswerOption start = option(2L, false);
        AnswerOption integer = option(4L, true);
        AnswerOption string = option(5L, false);
        AnswerOption bool = option(6L, true);

        when(answerOptionRepository.findByQuestionId(1L))
                .thenReturn(List.of(main, start));

        when(answerOptionRepository.findByQuestionId(2L))
                .thenReturn(List.of(integer, string, bool));
    }

    private AnswerOption option(Long id, boolean correct) {
        AnswerOption option = mock(AnswerOption.class);

        when(option.getId()).thenReturn(id);
        if (correct) {
            when(option.isCorrect()).thenReturn(true);
        }

        return option;
    }
}