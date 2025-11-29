package ru.mtuci.rbpo2025.service;

import org.springframework.stereotype.Service;
import ru.mtuci.rbpo2025.model.Delivery;
import ru.mtuci.rbpo2025.model.DeliveryStatus;
import ru.mtuci.rbpo2025.repository.DeliveryRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    public DeliveryService(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    public Delivery create(Delivery delivery) {
        delivery.setStatus(DeliveryStatus.CREATED);
        delivery.setCreatedAt(LocalDateTime.now());
        validateSlot(delivery);
        return deliveryRepository.save(delivery);
    }

    public Delivery getById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
    }

    public List<Delivery> getAll() {
        return deliveryRepository.findAll();
    }

    public Delivery update(Long id, Delivery updated) {
        Delivery existing = getById(id);

        existing.setParcelId(updated.getParcelId());
        existing.setCourierId(updated.getCourierId());
        existing.setStatus(updated.getStatus());
        existing.setDeliveryDate(updated.getDeliveryDate());
        existing.setSlotNumber(updated.getSlotNumber());
        existing.setAssignedAt(updated.getAssignedAt());
        existing.setDeliveredAt(updated.getDeliveredAt());

        validateSlot(existing);
        return deliveryRepository.save(existing);
    }

    public void delete(Long id) {
        deliveryRepository.deleteById(id);
    }

    private void validateSlot(Delivery delivery) {
        if (delivery.getCourierId() == null ||
                delivery.getDeliveryDate() == null ||
                delivery.getSlotNumber() == null) {
            return;
        }

        var sameSlot = deliveryRepository
                .findByCourierIdAndDeliveryDateAndSlotNumber(
                        delivery.getCourierId(),
                        delivery.getDeliveryDate(),
                        delivery.getSlotNumber()
                );

        boolean conflict = sameSlot.stream()
                .anyMatch(d -> !d.getId().equals(delivery.getId())
                        && d.getStatus() != DeliveryStatus.DELIVERED
                        && d.getStatus() != DeliveryStatus.CANCELED);

        if (conflict) {
            throw new RuntimeException("Courier already has active delivery in this slot");
        }
    }
}
