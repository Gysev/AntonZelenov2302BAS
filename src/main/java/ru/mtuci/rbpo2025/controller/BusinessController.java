package ru.mtuci.rbpo2025.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.rbpo2025.dto.*;
import ru.mtuci.rbpo2025.model.Delivery;
import ru.mtuci.rbpo2025.service.BusinessService;

import java.util.List;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @PostMapping("/orders")
    public ResponseEntity<Delivery> createOrder(@RequestBody CreateOrderRequest req) {
        return ResponseEntity.ok(businessService.createOrder(req));
    }

    @PostMapping("/couriers/unavailable/redistribute")
    public ResponseEntity<RedistributeDeliveriesResponse> redistributeDeliveries(@RequestBody RedistributeDeliveriesRequest req) {
        return ResponseEntity.ok(businessService.redistributeDeliveriesForUnavailableCourier(req));
    }

    @PostMapping("/deliveries/smart-assign")
    public ResponseEntity<Delivery> smartAssignCourier(@RequestBody SmartAssignCourierRequest req) {
        return ResponseEntity.ok(businessService.smartAssignCourier(req));
    }

    @PostMapping("/deliveries/schedule")
    public ResponseEntity<Delivery> scheduleDelivery(@RequestBody ScheduleDeliveryRequest req) {
        return ResponseEntity.ok(businessService.scheduleDelivery(req));
    }

    @PostMapping("/orders/partial-cancel")
    public ResponseEntity<PartialCancelResponse> partialCancelOrder(@RequestBody PartialCancelRequest req) {
        return ResponseEntity.ok(businessService.partialCancelOrder(req));
    }

    @PostMapping("/deliveries/check-sla")
    public ResponseEntity<CheckSlaResponse> checkAndMarkOverdue() {
        return ResponseEntity.ok(businessService.checkAndMarkOverdueDeliveries());
    }
}
