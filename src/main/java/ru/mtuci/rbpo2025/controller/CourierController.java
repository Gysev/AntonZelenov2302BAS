package ru.mtuci.rbpo2025.controller;

import org.springframework.web.bind.annotation.*;
import ru.mtuci.rbpo2025.model.Courier;
import ru.mtuci.rbpo2025.service.CourierService;

import java.util.List;

@RestController
@RequestMapping("/api/couriers")
public class CourierController {

    private final CourierService courierService;

    public CourierController(CourierService courierService) {
        this.courierService = courierService;
    }

    @PostMapping
    public Courier create(@RequestBody Courier courier) {
        return courierService.create(courier);
    }

    @GetMapping("/{id}")
    public Courier getById(@PathVariable Long id) {
        return courierService.getById(id);
    }

    @GetMapping
    public List<Courier> getAll() {
        return courierService.getAll();
    }

    @PutMapping("/{id}")
    public Courier update(@PathVariable Long id, @RequestBody Courier courier) {
        return courierService.update(id, courier);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        courierService.delete(id);
    }
}
