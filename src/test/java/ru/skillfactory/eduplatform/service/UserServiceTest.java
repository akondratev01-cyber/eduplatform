package ru.skillfactory.eduplatform.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.skillfactory.eduplatform.entity.User;
import ru.skillfactory.eduplatform.entity.enums.UserRole;
import ru.skillfactory.eduplatform.repository.UserRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerStudentShouldSaveNewStudent() {
        when(userRepository.existsByEmail("ivan@example.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.registerStudent(
                "Иван", "Петров",
                " IVAN@EXAMPLE.COM ", "password123"
        );

        assertEquals("Иван Петров", user.getName());
        assertEquals("ivan@example.com", user.getEmail());
        assertEquals("encoded-password", user.getPasswordHash());
        assertEquals(UserRole.STUDENT, user.getRole());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerStudentShouldRejectDuplicateEmail() {
        when(userRepository.existsByEmail("ivan@example.com"))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.registerStudent(
                        "Иван", "Петров",
                        "ivan@example.com", "password123"
                ));

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void registerStudentShouldRejectShortPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerStudent(
                        "Иван", "Петров",
                        "ivan@example.com", "123"
                ));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void getByIdShouldReturnExistingUser() {
        User user = new User(
                "Иван", "Петров",
                "ivan@example.com", "encoded-password",
                UserRole.STUDENT
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        User result = userService.getById(1L);

        assertSame(user, result);
        verify(userRepository).findById(1L);
    }

    @Test
    void getByIdShouldThrowWhenUserNotFound() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> userService.getById(999L));
    }
}