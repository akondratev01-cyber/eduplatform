package ru.skillfactory.eduplatform.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false, length = 50)
    private String type;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    private List<AnswerOption> answerOptions = new ArrayList<>();

    protected Question() {
    }

    public Question(Quiz quiz, String text, String type) {
        this.quiz = quiz;
        this.text = text;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<AnswerOption> getAnswerOptions() {
        return answerOptions;
    }
}