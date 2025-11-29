package ru.mtuci.rbpo2025.controller;

import org.springframework.web.bind.annotation.*;
import ru.mtuci.rbpo2025.model.Recipient;
import ru.mtuci.rbpo2025.service.RecipientService;

import java.util.List;

@RestController
@RequestMapping("/api/recipients")
public class RecipientController {

    private final RecipientService recipientService;

    public RecipientController(RecipientService recipientService) {
        this.recipientService = recipientService;
    }

    @PostMapping
    public Recipient create(@RequestBody Recipient recipient) {
        return recipientService.create(recipient);
    }

    @GetMapping("/{id}")
    public Recipient getById(@PathVariable Long id) {
        return recipientService.getById(id);
    }

    @GetMapping
    public List<Recipient> getAll() {
        return recipientService.getAll();
    }

    @PutMapping("/{id}")
    public Recipient update(@PathVariable Long id, @RequestBody Recipient recipient) {
        return recipientService.update(id, recipient);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        recipientService.delete(id);
    }
}
