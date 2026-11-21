package ru.skillfactory.eduplatform.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.skillfactory.eduplatform.entity.Quiz;
import ru.skillfactory.eduplatform.entity.QuizSubmission;
import ru.skillfactory.eduplatform.entity.User;
import ru.skillfactory.eduplatform.service.QuizService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class QuizControllerTest {

    @Mock
    private QuizService quizService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new QuizController(quizService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getStudentResultsShouldReturn200AndResults() throws Exception {
        QuizSubmission result = mock(QuizSubmission.class);
        Quiz quiz = mock(Quiz.class);
        User student = mock(User.class);

        when(result.getId()).thenReturn(10L);
        when(result.getQuiz()).thenReturn(quiz);
        when(result.getStudent()).thenReturn(student);
        when(result.getScore()).thenReturn(85);
        when(quiz.getId()).thenReturn(2L);
        when(student.getId()).thenReturn(1L);

        when(quizService.getStudentResults(1L))
                .thenReturn(List.of(result));

        mockMvc.perform(get("/api/students/1/quiz-results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].quizId").value(2))
                .andExpect(jsonPath("$[0].studentId").value(1))
                .andExpect(jsonPath("$[0].score").value(85));

        verify(quizService).getStudentResults(1L);
    }

    @Test
    void getQuizResultsShouldReturn200AndEmptyList() throws Exception {
        when(quizService.getQuizResults(2L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/quizzes/2/results"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(quizService).getQuizResults(2L);
    }

    @Test
    void getQuizResultsShouldReturn404WhenQuizNotFound() throws Exception {
        when(quizService.getQuizResults(999L))
                .thenThrow(new NoSuchElementException("Тест не найден"));

        mockMvc.perform(get("/api/quizzes/999/results"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Тест не найден"));

        verify(quizService).getQuizResults(999L);
    }
}