package ru.skillfactory.eduplatform.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_submissions")
public class QuizSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false, updatable = false)
    private LocalDateTime takenAt;

    protected QuizSubmission() {
    }

    public QuizSubmission(Quiz quiz, User student, int score) {
        this.quiz = quiz;
        this.student = student;
        this.score = score;
    }

    @PrePersist
    protected void onCreate() {
        if (takenAt == null) {
            takenAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public User getStudent() {
        return student;
    }

    public int getScore() {
        return score;
    }

    public LocalDateTime getTakenAt() {
        return takenAt;
    }
}