package ru.mtuci.rbpo2025.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.mtuci.rbpo2025.dto.*;
import ru.mtuci.rbpo2025.model.Courier;
import ru.mtuci.rbpo2025.model.Delivery;
import ru.mtuci.rbpo2025.model.DeliveryStatus;
import ru.mtuci.rbpo2025.model.Parcel;
import ru.mtuci.rbpo2025.model.Recipient;
import ru.mtuci.rbpo2025.model.Sender;
import ru.mtuci.rbpo2025.repository.CourierRepository;
import ru.mtuci.rbpo2025.repository.DeliveryRepository;
import ru.mtuci.rbpo2025.repository.ParcelRepository;
import ru.mtuci.rbpo2025.repository.RecipientRepository;
import ru.mtuci.rbpo2025.repository.SenderRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BusinessService {

    private static final Set<DeliveryStatus> ACTIVE_STATUSES = EnumSet.of(
            DeliveryStatus.CREATED,
            DeliveryStatus.ASSIGNED,
            DeliveryStatus.PICKED_UP,
            DeliveryStatus.IN_TRANSIT
    );

    private final SenderRepository senderRepository;
    private final RecipientRepository recipientRepository;
    private final CourierRepository courierRepository;
    private final ParcelRepository parcelRepository;
    private final DeliveryRepository deliveryRepository;

    public BusinessService(SenderRepository senderRepository,
                           RecipientRepository recipientRepository,
                           CourierRepository courierRepository,
                           ParcelRepository parcelRepository,
                           DeliveryRepository deliveryRepository) {
        this.senderRepository = senderRepository;
        this.recipientRepository = recipientRepository;
        this.courierRepository = courierRepository;
        this.parcelRepository = parcelRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional
    public Delivery createOrder(CreateOrderRequest req) {
        Sender sender = senderRepository.findById(req.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        Recipient recipient = recipientRepository.findById(req.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));
        Courier courier = courierRepository.findById(req.getCourierId())
                .orElseThrow(() -> new RuntimeException("Courier not found"));

        Parcel parcel = new Parcel();
        parcel.setSenderId(sender.getId());
        parcel.setRecipientId(recipient.getId());
        parcel = parcelRepository.save(parcel);

        Delivery delivery = new Delivery();
        delivery.setParcelId(parcel.getId());
        delivery.setCourierId(courier.getId());
        delivery.setStatus(DeliveryStatus.CREATED);
        delivery.setCreatedAt(LocalDateTime.now());
        delivery.setIsOverdue(false);
        delivery.setSlaDeadline(LocalDateTime.now().plusDays(3));
        return deliveryRepository.save(delivery);
    }

    @Transactional
    public Delivery assignCourier(Long deliveryId, AssignCourierRequest req) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        Courier courier = courierRepository.findById(req.getCourierId())
                .orElseThrow(() -> new RuntimeException("Courier not found"));

        delivery.setCourierId(courier.getId());
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedAt(LocalDateTime.now());
        if (delivery.getSlaDeadline() == null) {
            delivery.setSlaDeadline(LocalDateTime.now().plusDays(2));
        }
        return deliveryRepository.save(delivery);
    }

    @Transactional
    public RedistributeDeliveriesResponse redistributeDeliveriesForUnavailableCourier(RedistributeDeliveriesRequest req) {
        if (req == null || req.getUnavailableCourierId() == null) {
            throw new RuntimeException("unavailableCourierId is required");
        }

        Long unavailableCourierId = req.getUnavailableCourierId();
        Courier unavailableCourier = courierRepository.findById(unavailableCourierId)
                .orElseThrow(() -> new RuntimeException("Courier not found"));

        unavailableCourier.setIsAvailable(false);
        courierRepository.save(unavailableCourier);

        List<Delivery> activeDeliveries = deliveryRepository.findByCourierIdAndStatusIn(
                unavailableCourierId, ACTIVE_STATUSES);

        if (activeDeliveries.isEmpty()) {
            return new RedistributeDeliveriesResponse(
                    unavailableCourierId, 0, new ArrayList<>(),
                    "No active deliveries to redistribute for courier " + unavailableCourierId);
        }

        List<Long> redistributedIds = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Delivery delivery : activeDeliveries) {
            List<Courier> availableCouriers = courierRepository.findByIsAvailableTrueOrderByCurrentLoadAsc();
            
            if (availableCouriers.isEmpty()) {
                delivery.setCourierId(null);
                delivery.setStatus(DeliveryStatus.CREATED);
            } else {
                Courier newCourier = availableCouriers.get(0);
                delivery.setCourierId(newCourier.getId());
                delivery.setAssignedAt(now);
                
                if (newCourier.getCurrentLoad() == null) {
                    newCourier.setCurrentLoad(0);
                }
                newCourier.setCurrentLoad(newCourier.getCurrentLoad() + 1);
                courierRepository.save(newCourier);
            }
            
            deliveryRepository.save(delivery);
            redistributedIds.add(delivery.getId());
        }

        unavailableCourier.setCurrentLoad(0);
        courierRepository.save(unavailableCourier);

        String reason = req.getReason() != null ? req.getReason() : "Courier unavailable";
        return new RedistributeDeliveriesResponse(
                unavailableCourierId,
                redistributedIds.size(),
                redistributedIds,
                String.format("Redistributed %d deliveries. Reason: %s", redistributedIds.size(), reason)
        );
    }

    @Transactional
    public Delivery smartAssignCourier(SmartAssignCourierRequest req) {
        if (req == null || req.getDeliveryId() == null) {
            throw new RuntimeException("deliveryId is required");
        }

        Delivery delivery = deliveryRepository.findById(req.getDeliveryId())
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (delivery.getCourierId() != null) {
            throw new RuntimeException("Delivery already has a courier assigned");
        }

        if (delivery.getStatus() != DeliveryStatus.CREATED && delivery.getStatus() != DeliveryStatus.SCHEDULED) {
            throw new RuntimeException("Cannot assign courier to delivery in status: " + delivery.getStatus());
        }

        List<Courier> availableCouriers = courierRepository.findByIsAvailableTrueOrderByCurrentLoadAsc();

        if (availableCouriers.isEmpty()) {
            throw new RuntimeException("No available couriers");
        }

        Courier selectedCourier = null;
        for (Courier courier : availableCouriers) {
            int currentLoad = courier.getCurrentLoad() != null ? courier.getCurrentLoad() : 0;
            int maxLoad = courier.getMaxLoad() != null ? courier.getMaxLoad() : 5;

            if (currentLoad < maxLoad) {
                selectedCourier = courier;
                break;
            }
        }

        if (selectedCourier == null) {
            throw new RuntimeException("All available couriers are at maximum load");
        }

        delivery.setCourierId(selectedCourier.getId());
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedAt(LocalDateTime.now());

        if (selectedCourier.getCurrentLoad() == null) {
            selectedCourier.setCurrentLoad(0);
        }
        selectedCourier.setCurrentLoad(selectedCourier.getCurrentLoad() + 1);
        courierRepository.save(selectedCourier);

        return deliveryRepository.save(delivery);
    }

    @Transactional
    public Delivery scheduleDelivery(ScheduleDeliveryRequest req) {
        if (req == null || req.getDeliveryId() == null || req.getScheduledFor() == null) {
            throw new RuntimeException("deliveryId and scheduledFor are required");
        }

        Delivery delivery = deliveryRepository.findById(req.getDeliveryId())
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.CREATED) {
            throw new RuntimeException("Can only schedule deliveries in CREATED status");
        }

        if (req.getScheduledFor().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Scheduled time must be in the future");
        }

        delivery.setStatus(DeliveryStatus.SCHEDULED);
        delivery.setScheduledFor(req.getScheduledFor());

        return deliveryRepository.save(delivery);
    }

    @Transactional
    public CancelOrderResponse cancelOrder(Long orderId, CancelOrderRequest req) {
        Delivery delivery = deliveryRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel a delivered order");
        }

        if (delivery.getStatus() == DeliveryStatus.CANCELED || 
            delivery.getStatus() == DeliveryStatus.PARTIALLY_CANCELED) {
            return new CancelOrderResponse(orderId, delivery.getStatus().name(), "Order already canceled");
        }

        if (delivery.getStatus() == DeliveryStatus.PICKED_UP || delivery.getStatus() == DeliveryStatus.IN_TRANSIT) {
            throw new RuntimeException("Cannot cancel order after pickup. Order is already in transit.");
        }

        Long oldCourierId = delivery.getCourierId();
        if (oldCourierId != null) {
            Courier courier = courierRepository.findById(oldCourierId)
                    .orElseThrow(() -> new RuntimeException("Courier not found"));
            
            if (courier.getCurrentLoad() != null && courier.getCurrentLoad() > 0) {
                courier.setCurrentLoad(courier.getCurrentLoad() - 1);
                courierRepository.save(courier);
            }
            
            delivery.setCourierId(null);
        }

        delivery.setStatus(DeliveryStatus.CANCELED);
        deliveryRepository.save(delivery);

        String reason = req == null ? null : req.getReason();
        String message = (reason == null || reason.isBlank())
                ? "Order canceled" + (oldCourierId != null ? ". Courier has been released." : "")
                : "Order canceled: " + reason + (oldCourierId != null ? " Courier has been released." : "");

        return new CancelOrderResponse(orderId, DeliveryStatus.CANCELED.name(), message);
    }

    @Transactional
    public PartialCancelResponse partialCancelOrder(PartialCancelRequest req) {
        if (req == null || req.getOrderId() == null) {
            throw new RuntimeException("orderId is required");
        }

        Delivery delivery = deliveryRepository.findById(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel a delivered order");
        }

        if (delivery.getStatus() == DeliveryStatus.PARTIALLY_CANCELED || 
            delivery.getStatus() == DeliveryStatus.CANCELED) {
            return new PartialCancelResponse(
                    req.getOrderId(),
                    delivery.getStatus().name(),
                    delivery.getCancellationCompensation(),
                    "Order already canceled or partially canceled"
            );
        }

        if (delivery.getStatus() == DeliveryStatus.PICKED_UP || 
            delivery.getStatus() == DeliveryStatus.IN_TRANSIT) {
            throw new RuntimeException("Cannot cancel order after pickup. Order is already in transit.");
        }

        Double compensation = req.getCompensationAmount() != null ? req.getCompensationAmount() : 0.0;
        delivery.setCancellationCompensation(compensation);
        delivery.setCancellationReason(req.getReason());

        Long oldCourierId = delivery.getCourierId();
        if (oldCourierId != null) {
            Courier courier = courierRepository.findById(oldCourierId)
                    .orElseThrow(() -> new RuntimeException("Courier not found"));
            
            if (courier.getCurrentLoad() != null && courier.getCurrentLoad() > 0) {
                courier.setCurrentLoad(courier.getCurrentLoad() - 1);
                courierRepository.save(courier);
            }
            
            delivery.setCourierId(null);
        }

        delivery.setStatus(DeliveryStatus.PARTIALLY_CANCELED);
        deliveryRepository.save(delivery);

        String message = String.format("Order partially canceled. Compensation: %.2f. Reason: %s",
                compensation, req.getReason() != null ? req.getReason() : "Not specified");

        return new PartialCancelResponse(
                req.getOrderId(),
                DeliveryStatus.PARTIALLY_CANCELED.name(),
                compensation,
                message
        );
    }

    @Transactional
    public CheckSlaResponse checkAndMarkOverdueDeliveries() {
        LocalDateTime now = LocalDateTime.now();
        
        List<Delivery> overdueDeliveries = deliveryRepository.findByStatusInAndSlaDeadlineBefore(
                ACTIVE_STATUSES, now);

        List<Long> overdueIds = new ArrayList<>();
        
        for (Delivery delivery : overdueDeliveries) {
            if (delivery.getSlaDeadline() != null && delivery.getSlaDeadline().isBefore(now)) {
                delivery.setIsOverdue(true);
                delivery.setStatus(DeliveryStatus.OVERDUE);
                deliveryRepository.save(delivery);
                overdueIds.add(delivery.getId());
            }
        }

        List<Delivery> allActive = deliveryRepository.findAll().stream()
                .filter(d -> ACTIVE_STATUSES.contains(d.getStatus()))
                .filter(d -> d.getSlaDeadline() == null)
                .collect(Collectors.toList());

        String message = String.format(
                "Checked %d active deliveries. Found %d overdue deliveries. %d deliveries without SLA deadline.",
                overdueDeliveries.size() + allActive.size(),
                overdueIds.size(),
                allActive.size()
        );

        return new CheckSlaResponse(
                overdueDeliveries.size() + allActive.size(),
                overdueIds.size(),
                overdueIds,
                message
        );
    }

    @Transactional
    public Delivery setDeliveryStatus(Long deliveryId, SetDeliveryStatusRequest req) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        delivery.setStatus(req.getStatus());
        return deliveryRepository.save(delivery);
    }

    @Transactional
    public List<Delivery> activateScheduledDeliveries() {
        LocalDateTime now = LocalDateTime.now();
        
        List<Delivery> scheduledDeliveries = deliveryRepository.findByStatusAndScheduledForBefore(
                DeliveryStatus.SCHEDULED, now);

        List<Delivery> activated = new ArrayList<>();
        
        for (Delivery delivery : scheduledDeliveries) {
            delivery.setStatus(DeliveryStatus.CREATED);
            delivery.setScheduledFor(null);
            deliveryRepository.save(delivery);
            activated.add(delivery);
        }

        return activated;
    }

    @Transactional
    public Delivery completeDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(LocalDateTime.now());
        
        if (delivery.getCourierId() != null) {
            Courier courier = courierRepository.findById(delivery.getCourierId())
                    .orElseThrow(() -> new RuntimeException("Courier not found"));
            
            if (courier.getCurrentLoad() != null && courier.getCurrentLoad() > 0) {
                courier.setCurrentLoad(courier.getCurrentLoad() - 1);
                courierRepository.save(courier);
            }
        }
        
        return deliveryRepository.save(delivery);
    }

    public List<Delivery> getCourierDeliveries(Long courierId) {
        courierRepository.findById(courierId)
                .orElseThrow(() -> new RuntimeException("Courier not found"));
        return deliveryRepository.findByCourierId(courierId);
    }
}

