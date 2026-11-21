package ru.skillfactory.eduplatform.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillfactory.eduplatform.entity.User;
import ru.skillfactory.eduplatform.entity.enums.UserRole;
import ru.skillfactory.eduplatform.repository.UserRepository;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Регистрация студента
    public User registerStudent(String firstName, String lastName, String email, String rawPassword) {
        validateRegistration(firstName, lastName, email, rawPassword);
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Пользователь с email " + normalizedEmail + " уже зарегистрирован");
        }
        String password = passwordEncoder.encode(rawPassword);

        User user = new User(firstName.trim(), lastName.trim(), normalizedEmail, password, UserRole.STUDENT);
        return userRepository.save(user);
    }

    // Получение пользователя по ID
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + id + " не найден"));
    }

    // Получение всех пользователей
    @Transactional(readOnly = true)
    public List<User> getAll() {
        return userRepository.findAll();
    }

    // Поиск по электронной почте
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);

        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с email " + normalizedEmail + " не найден"));
    }

    // Изменение имени пользователя
    public User updateName(Long id, String firstName, String lastName) {
        validateName(firstName, lastName);
        User user = getById(id);
        user.setName(firstName.trim(), lastName.trim());
        return userRepository.save(user);
    }

    // Изменение электронной почты
    public User updateEmail(Long id, String newEmail) {
        if (newEmail == null || newEmail.isBlank()) {
            throw new IllegalArgumentException("Email не указан");
        }

        String normalizedEmail = normalizeEmail(newEmail);

        User user = getById(id);
        if (!user.getEmail().equals(normalizedEmail) && userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }
        user.setEmail(normalizedEmail);
        return userRepository.save(user);
    }

    // Удаление пользователя
    public void delete(Long id) {
        User user = getById(id);
        userRepository.delete(user);
    }

    // Проверка пароля без раскрытия хеша
    @Transactional(readOnly = true)
    public boolean verifyPassword(Long id, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Пароль не указан");
        }

        User user = getById(id);
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    private void validateRegistration(String firstName, String lastName, String email, String rawPassword) {
        validateName(firstName, lastName);

        if (email == null || email.isBlank() || !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Укажите корректный email");
        }

        if (rawPassword == null || rawPassword.length() < 8) {
            throw new IllegalArgumentException("Пароль должен быть не менее 8 символов");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank() || !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Укажите корректный email");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validateName(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Имя и фамилия обязательны");
        }

        String fullName = firstName.trim() + " " + lastName.trim();

        if (fullName.length() > 200) {
            throw new IllegalArgumentException("Имя и фамилия слишком длинные");
        }
    }
}
