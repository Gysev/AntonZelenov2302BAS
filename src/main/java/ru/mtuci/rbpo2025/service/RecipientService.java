package ru.mtuci.rbpo2025.service;

import org.springframework.stereotype.Service;
import ru.mtuci.rbpo2025.model.Recipient;
import ru.mtuci.rbpo2025.repository.RecipientRepository;

import java.util.List;

@Service
public class RecipientService {

    private final RecipientRepository recipientRepository;

    public RecipientService(RecipientRepository recipientRepository) {
        this.recipientRepository = recipientRepository;
    }

    public Recipient create(Recipient recipient) {
        return recipientRepository.save(recipient);
    }

    public Recipient getById(Long id) {
        return recipientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));
    }

    public List<Recipient> getAll() {
        return recipientRepository.findAll();
    }

    public Recipient update(Long id, Recipient updated) {
        Recipient existing = getById(id);
        existing.setName(updated.getName());
        existing.setPhone(updated.getPhone());
        existing.setAddress(updated.getAddress());
        return recipientRepository.save(existing);
    }

    public void delete(Long id) {
        recipientRepository.deleteById(id);
    }
}
