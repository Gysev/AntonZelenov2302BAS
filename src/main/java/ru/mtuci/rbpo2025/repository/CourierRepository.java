package ru.mtuci.rbpo2025.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mtuci.rbpo2025.model.Courier;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {
}
