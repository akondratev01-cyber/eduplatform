package ru.skillfactory.eduplatform.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.skillfactory.eduplatform.entity.Course;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacherId(Long teacherId);

    List<Course> findByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"modules"})
    Optional<Course> findWithModulesById(Long id);
}