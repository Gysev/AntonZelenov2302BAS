package ru.mtuci.rbpo2025.controller;

import org.springframework.web.bind.annotation.*;
import ru.mtuci.rbpo2025.model.Parcel;
import ru.mtuci.rbpo2025.service.ParcelService;

import java.util.List;

@RestController
@RequestMapping("/api/parcels")
public class ParcelController {

    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @PostMapping
    public Parcel create(@RequestBody Parcel parcel) {
        return parcelService.create(parcel);
    }

    @GetMapping("/{id}")
    public Parcel getById(@PathVariable Long id) {
        return parcelService.getById(id);
    }

    @GetMapping
    public List<Parcel> getAll() {
        return parcelService.getAll();
    }

    @PutMapping("/{id}")
    public Parcel update(@PathVariable Long id, @RequestBody Parcel parcel) {
        return parcelService.update(id, parcel);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        parcelService.delete(id);
    }
}
