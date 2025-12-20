package ru.mtuci.rbpo2025.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.rbpo2025.model.Delivery;
import ru.mtuci.rbpo2025.model.DeliveryStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    List<Delivery> findByCourierId(Long courierId);

    List<Delivery> findByCourierIdAndDeliveryDateAndSlotNumber(Long courierId, LocalDate deliveryDate, Integer slotNumber);

    Optional<Delivery> findFirstByCourierIdAndStatusOrderByDeliveryDateAscSlotNumberAscCreatedAtAsc(Long courierId, DeliveryStatus status);

    Optional<Delivery> findFirstByCourierIdIsNullAndStatusOrderByDeliveryDateAscSlotNumberAscCreatedAtAsc(DeliveryStatus status);

    boolean existsByCourierIdAndStatusIn(Long courierId, Collection<DeliveryStatus> statuses);

    boolean existsByCourierIdAndDeliveryDateAndSlotNumberAndStatusInAndIdNot(
            Long courierId,
            LocalDate deliveryDate,
            Integer slotNumber,
            Collection<DeliveryStatus> statuses,
            Long id
    );

    List<Delivery> findByCourierIdAndStatusIn(Long courierId, Collection<DeliveryStatus> statuses);

    List<Delivery> findByStatusAndScheduledForBefore(DeliveryStatus status, LocalDateTime before);

    List<Delivery> findByStatusInAndSlaDeadlineBefore(Collection<DeliveryStatus> statuses, LocalDateTime deadline);

    List<Delivery> findByCourierIdIsNullAndStatus(DeliveryStatus status);
}
