package ru.skillfactory.eduplatform.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateCourseRequestValidationTest {

    @Test
    void shouldRejectCourseTitleLongerThan200Characters() {
        try (ValidatorFactory factory =
                     Validation.buildDefaultValidatorFactory()) {

            Validator validator = factory.getValidator();

            CreateCourseRequest request = new CreateCourseRequest(
                    "A".repeat(201),
                    "Описание курса",
                    1L,
                    1L,
                    40,
                    LocalDate.now().plusDays(1)
            );

            var violations = validator.validate(request);

            assertTrue(
                    violations.stream().anyMatch(violation ->
                            violation.getPropertyPath().toString().equals("title")
                    ),
                    "Название курса длиннее 200 символов должно отклоняться"
            );
        }
    }
}