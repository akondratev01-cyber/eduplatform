package ru.skillfactory.eduplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skillfactory.eduplatform.entity.Profile;

import java.util.Optional;

public interface ProfileRepositor extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUserId(Long userId);
}
