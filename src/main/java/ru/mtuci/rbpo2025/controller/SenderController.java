package ru.mtuci.rbpo2025.controller;

import org.springframework.web.bind.annotation.*;
import ru.mtuci.rbpo2025.model.Sender;
import ru.mtuci.rbpo2025.service.SenderService;

import java.util.List;

@RestController
@RequestMapping("/api/senders")
public class SenderController {

    private final SenderService senderService;

    public SenderController(SenderService senderService) {
        this.senderService = senderService;
    }

    @PostMapping
    public Sender create(@RequestBody Sender sender) {
        return senderService.create(sender);
    }

    @GetMapping("/{id}")
    public Sender getById(@PathVariable Long id) {
        return senderService.getById(id);
    }

    @GetMapping
    public List<Sender> getAll() {
        return senderService.getAll();
    }

    @PutMapping("/{id}")
    public Sender update(@PathVariable Long id, @RequestBody Sender sender) {
        return senderService.update(id, sender);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        senderService.delete(id);
    }
}
