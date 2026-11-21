package ru.skillfactory.eduplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skillfactory.eduplatform.entity.Module;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findByCourseIdOrderByOrderIndexAsc(Long courseId);
}
