package ru.mtuci.rbpo2025.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.rbpo2025.model.AppUser;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
}
