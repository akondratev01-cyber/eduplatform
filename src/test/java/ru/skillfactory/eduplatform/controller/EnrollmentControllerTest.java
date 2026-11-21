package ru.skillfactory.eduplatform.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.skillfactory.eduplatform.service.EnrollmentService;

import java.util.NoSuchElementException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EnrollmentControllerTest {

    @Mock
    private EnrollmentService enrollmentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new EnrollmentController(enrollmentService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void unenrollShouldReturn204() throws Exception {
        mockMvc.perform(delete(
                        "/api/enrollments/students/1/courses/2"))
                .andExpect(status().isNoContent());

        verify(enrollmentService).unenroll(1L, 2L);
    }

    @Test
    void unenrollShouldReturn404WhenEnrollmentNotFound() throws Exception {
        doThrow(new NoSuchElementException(
                "Запись студента на курс не найдена"))
                .when(enrollmentService).unenroll(1L, 999L);

        mockMvc.perform(delete(
                        "/api/enrollments/students/1/courses/999"))
                .andExpect(status().isNotFound());

        verify(enrollmentService).unenroll(1L, 999L);
    }
}