package ru.mtuci.rbpo2025.service;

import org.springframework.stereotype.Service;
import ru.mtuci.rbpo2025.model.Courier;
import ru.mtuci.rbpo2025.repository.CourierRepository;

import java.util.List;

@Service
public class CourierService {

    private final CourierRepository courierRepository;

    public CourierService(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    public Courier create(Courier courier) {
        if (courier.getIsAvailable() == null) {
            courier.setIsAvailable(true);
        }
        if (courier.getCurrentLoad() == null) {
            courier.setCurrentLoad(0);
        }
        if (courier.getMaxLoad() == null) {
            courier.setMaxLoad(5);
        }
        return courierRepository.save(courier);
    }

    public Courier getById(Long id) {
        return courierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Courier not found"));
    }

    public List<Courier> getAll() {
        return courierRepository.findAll();
    }

    public Courier update(Long id, Courier updated) {
        Courier existing = getById(id);
        existing.setName(updated.getName());
        existing.setPhone(updated.getPhone());
        if (updated.getIsAvailable() != null) {
            existing.setIsAvailable(updated.getIsAvailable());
        }
        if (updated.getCurrentLoad() != null) {
            existing.setCurrentLoad(updated.getCurrentLoad());
        }
        if (updated.getMaxLoad() != null) {
            existing.setMaxLoad(updated.getMaxLoad());
        }
        return courierRepository.save(existing);
    }

    public void delete(Long id) {
        courierRepository.deleteById(id);
    }
}
