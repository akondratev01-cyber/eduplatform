package ru.skillfactory.eduplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skillfactory.eduplatform.entity.Lesson;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByModuleId(Long id);
}
