package ru.mtuci.rbpo2025.service;

import org.springframework.stereotype.Service;
import ru.mtuci.rbpo2025.model.Parcel;
import ru.mtuci.rbpo2025.repository.ParcelRepository;

import java.util.List;

@Service
public class ParcelService {

    private final ParcelRepository parcelRepository;

    public ParcelService(ParcelRepository parcelRepository) {
        this.parcelRepository = parcelRepository;
    }

    public Parcel create(Parcel parcel) {
        return parcelRepository.save(parcel);
    }

    public Parcel getById(Long id) {
        return parcelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parcel not found"));
    }

    public List<Parcel> getAll() {
        return parcelRepository.findAll();
    }

    public Parcel update(Long id, Parcel updated) {
        Parcel existing = getById(id);
        existing.setTrackingNumber(updated.getTrackingNumber());
        existing.setDescription(updated.getDescription());
        existing.setWeightKg(updated.getWeightKg());
        existing.setSenderId(updated.getSenderId());
        existing.setRecipientId(updated.getRecipientId());
        return parcelRepository.save(existing);
    }

    public void delete(Long id) {
        parcelRepository.deleteById(id);
    }
}
