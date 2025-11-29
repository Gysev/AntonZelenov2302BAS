package ru.mtuci.rbpo2025.service;

import org.springframework.stereotype.Service;
import ru.mtuci.rbpo2025.model.Sender;
import ru.mtuci.rbpo2025.repository.SenderRepository;

import java.util.List;

@Service
public class SenderService {

    private final SenderRepository senderRepository;

    public SenderService(SenderRepository senderRepository) {
        this.senderRepository = senderRepository;
    }

    public Sender create(Sender sender) {
        return senderRepository.save(sender);
    }

    public Sender getById(Long id) {
        return senderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
    }

    public List<Sender> getAll() {
        return senderRepository.findAll();
    }

    public Sender update(Long id, Sender updated) {
        Sender existing = getById(id);
        existing.setName(updated.getName());
        existing.setPhone(updated.getPhone());
        existing.setAddress(updated.getAddress());
        return senderRepository.save(existing);
    }

    public void delete(Long id) {
        senderRepository.deleteById(id);
    }
}
