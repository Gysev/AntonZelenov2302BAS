package ru.mtuci.rbpo2025.controller;

import org.springframework.web.bind.annotation.*;
import ru.mtuci.rbpo2025.model.Delivery;
import ru.mtuci.rbpo2025.service.BusinessService;
import ru.mtuci.rbpo2025.service.DeliveryService;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final BusinessService businessService;

    public DeliveryController(DeliveryService deliveryService, BusinessService businessService) {
        this.deliveryService = deliveryService;
        this.businessService = businessService;
    }

    @PostMapping
    public Delivery create(@RequestBody Delivery delivery) {
        return deliveryService.create(delivery);
    }

    @GetMapping("/{id}")
    public Delivery getById(@PathVariable Long id) {
        return deliveryService.getById(id);
    }

    @GetMapping
    public List<Delivery> getAll() {
        return deliveryService.getAll();
    }

    @PutMapping("/{id}")
    public Delivery update(@PathVariable Long id, @RequestBody Delivery delivery) {
        return deliveryService.update(id, delivery);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        deliveryService.delete(id);
    }

    @PostMapping("/{id}/complete")
    public Delivery completeDelivery(@PathVariable Long id) {
        return businessService.completeDelivery(id);
    }
}
