package ru.skillfactory.eduplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skillfactory.eduplatform.entity.Quiz;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByModuleId(Long moduleid);
}