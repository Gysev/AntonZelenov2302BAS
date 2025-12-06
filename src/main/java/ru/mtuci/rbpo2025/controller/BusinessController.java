package ru.mtuci.rbpo2025.controller;

import org.springframework.web.bind.annotation.*;
import ru.mtuci.rbpo2025.dto.AssignCourierRequest;
import ru.mtuci.rbpo2025.dto.CreateOrderRequest;
import ru.mtuci.rbpo2025.dto.SetDeliveryStatusRequest;
import ru.mtuci.rbpo2025.model.Delivery;
import ru.mtuci.rbpo2025.service.BusinessService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @PostMapping("/orders")
    public Delivery createOrder(@RequestBody CreateOrderRequest req) {
        return businessService.createOrder(req);
    }

    @PatchMapping("/deliveries/{id}/assign")
    public Delivery assignCourier(@PathVariable Long id, @RequestBody AssignCourierRequest req) {
        return businessService.assignCourier(id, req);
    }

    @PatchMapping("/deliveries/{id}/status")
    public Delivery setDeliveryStatus(@PathVariable Long id, @RequestBody SetDeliveryStatusRequest req) {
        return businessService.setDeliveryStatus(id, req);
    }

    @PostMapping("/deliveries/{id}/complete")
    public Delivery complete(@PathVariable Long id) {
        return businessService.completeDelivery(id);
    }

    @GetMapping("/couriers/{id}/deliveries")
    public List<Delivery> getCourierDeliveries(@PathVariable Long id) {
        return businessService.getCourierDeliveries(id);
    }
}
