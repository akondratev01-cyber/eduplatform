package ru.skillfactory.eduplatform.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.skillfactory.eduplatform.entity.*;
import ru.skillfactory.eduplatform.entity.Module;
import ru.skillfactory.eduplatform.entity.enums.UserRole;
import ru.skillfactory.eduplatform.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Profile("demo")
public class DemoDataLoader implements CommandLineRunner {

    private final UserRepository users;
    private final CategoryRepository categories;
    private final CourseRepository courses;
    private final ModuleRepository modules;
    private final LessonRepository lessons;
    private final AssignmentRepository assignments;
    private final QuizRepository quizzes;
    private final QuestionRepository questions;
    private final AnswerOptionRepository options;
    private final EnrollmentRepository enrollments;
    private final SubmissionRepository submissions;
    private final QuizSubmissionRepository quizSubmissions;
    private final PasswordEncoder passwordEncoder;

    public DemoDataLoader(
            UserRepository users,
            CategoryRepository categories,
            CourseRepository courses,
            ModuleRepository modules,
            LessonRepository lessons,
            AssignmentRepository assignments,
            QuizRepository quizzes,
            QuestionRepository questions,
            AnswerOptionRepository options,
            EnrollmentRepository enrollments,
            SubmissionRepository submissions,
            QuizSubmissionRepository quizSubmissions,
            PasswordEncoder passwordEncoder
    ) {
        this.users = users;
        this.categories = categories;
        this.courses = courses;
        this.modules = modules;
        this.lessons = lessons;
        this.assignments = assignments;
        this.quizzes = quizzes;
        this.questions = questions;
        this.options = options;
        this.enrollments = enrollments;
        this.submissions = submissions;
        this.quizSubmissions = quizSubmissions;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        // Do not insert demonstration data twice.
        if (users.existsByEmail("demo.teacher@example.com")) {
            System.out.println("Demo data already exists. Skipping.");
            return;
        }

        String passwordHash = passwordEncoder.encode("Demo12345!");

        User teacher = users.save(new User(
                "Алексей", "Смирнов",
                "demo.teacher@example.com",
                passwordHash,
                UserRole.TEACHER
        ));

        User student = users.save(new User(
                "Иван", "Петров",
                "demo.student@example.com",
                passwordHash,
                UserRole.STUDENT
        ));

        User secondStudent = users.save(new User(
                "Пётр", "Сидоров",
                "demo.student2@example.com",
                passwordHash,
                UserRole.STUDENT
        ));

        Category category = categories.save(
                new Category("Программирование")
        );

        Course course = courses.save(new Course(
                "Основы Java",
                "Введение в язык Java и основы программирования",
                teacher,
                category,
                40,
                LocalDate.of(2026, 12, 1)
        ));

        Module module = modules.save(new Module(
                course,
                "Введение в Java",
                0,
                "Знакомство с языком и инструментами разработки"
        ));

        Lesson lesson = lessons.save(new Lesson(
                module,
                "Установка JDK и IntelliJ IDEA",
                "Установите JDK 21 и настройте IntelliJ IDEA.",
                null
        ));

        Assignment assignment = new Assignment(
                lesson,
                "Первая программа на Java",
                "Напишите программу Hello, World!",
                100
        );
        assignment.setDueDate(
                LocalDateTime.of(2026, 12, 15, 23, 59)
        );
        assignment = assignments.save(assignment);

        enrollments.save(new Enrollment(student, course));
        enrollments.save(new Enrollment(secondStudent, course));

        Submission submission = new Submission(
                assignment,
                student,
                "System.out.println(\"Hello, World!\");"
        );
        submission.setScore(95);
        submission.setFeedback("Задание выполнено правильно.");
        submissions.save(submission);

        Quiz quiz = quizzes.save(new Quiz(
                module,
                "Основы синтаксиса Java",
                30
        ));

        Question first = questions.save(new Question(
                quiz,
                "Какой метод является точкой входа в Java-программу?",
                "SINGLE_CHOICE"
        ));

        options.save(new AnswerOption(first, "main()", true));
        options.save(new AnswerOption(first, "start()", false));
        options.save(new AnswerOption(first, "run()", false));

        Question second = questions.save(new Question(
                quiz,
                "Какие из перечисленных типов являются примитивными в Java?",
                "MULTIPLE_CHOICE"
        ));

        options.save(new AnswerOption(second, "int", true));
        options.save(new AnswerOption(second, "String", false));
        options.save(new AnswerOption(second, "boolean", true));
        options.save(new AnswerOption(second, "Integer", false));

        quizSubmissions.save(
                new QuizSubmission(quiz, student, 100)
        );

        System.out.println("EduPlatform demo data loaded successfully.");
    }
}