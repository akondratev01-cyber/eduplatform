package ru.skillfactory.eduplatform.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.skillfactory.eduplatform.service.AssignmentService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AssignmentControllerTest {

    @Mock
    private AssignmentService assignmentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AssignmentController(assignmentService)).setControllerAdvice(
                new GlobalExceptionHandler()).build();
    }

    @Test
    void getStudentSubmissionsShouldReturn200() throws Exception {
        when(assignmentService.getStudentSubmissions(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/students/1/submissions")).andExpect(status().isOk()).andExpect(content().json("[]"));

        verify(assignmentService).getStudentSubmissions(1L);
    }

    @Test
    void getAssignmentSubmissionsShouldReturn200() throws Exception {
        when(assignmentService.getAssignmentSubmissions(5L)).thenReturn(List.of());

        mockMvc.perform(get("/api/assignments/5/submissions")).andExpect(status().isOk()).andExpect(
                content().json("[]"));

        verify(assignmentService).getAssignmentSubmissions(5L);
    }

    @Test
    void getAssignmentSubmissionsShouldReturn404() throws Exception {
        when(assignmentService.getAssignmentSubmissions(999L)).thenThrow(
                new NoSuchElementException("Задание с ID 999 не найдено"));

        mockMvc.perform(get("/api/assignments/999/submissions")).andExpect(status().isNotFound()).andExpect(
                jsonPath("$.error").value("Задание с ID 999 не найдено"));

        verify(assignmentService).getAssignmentSubmissions(999L);
    }
}