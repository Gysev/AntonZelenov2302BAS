package ru.mtuci.rbpo2025.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mtuci.rbpo2025.model.Delivery;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    List<Delivery> findByCourierIdAndDeliveryDateAndSlotNumber(Long courierId,
                                                               LocalDate deliveryDate,
                                                               Integer slotNumber);
}
