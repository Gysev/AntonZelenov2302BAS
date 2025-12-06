package ru.mtuci.rbpo2025.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.rbpo2025.model.Delivery;

import java.time.LocalDate;
import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByCourierId(Long courierId);
    List<Delivery> findByCourierIdAndDeliveryDateAndSlotNumber(Long courierId, LocalDate deliveryDate, Integer slotNumber);
}
