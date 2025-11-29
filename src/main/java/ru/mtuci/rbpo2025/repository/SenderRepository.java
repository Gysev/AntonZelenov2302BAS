package ru.mtuci.rbpo2025.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mtuci.rbpo2025.model.Sender;

@Repository
public interface SenderRepository extends JpaRepository<Sender, Long> {
}
