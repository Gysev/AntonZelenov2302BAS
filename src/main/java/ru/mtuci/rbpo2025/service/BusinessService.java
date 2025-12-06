package ru.mtuci.rbpo2025.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.mtuci.rbpo2025.dto.AssignCourierRequest;
import ru.mtuci.rbpo2025.dto.CreateOrderRequest;
import ru.mtuci.rbpo2025.dto.SetDeliveryStatusRequest;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BusinessService {

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
        return deliveryRepository.save(delivery);
    }

    @Transactional
    public Delivery setDeliveryStatus(Long deliveryId, SetDeliveryStatusRequest req) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        delivery.setStatus(req.getStatus());
        return deliveryRepository.save(delivery);
    }

    @Transactional
    public Delivery completeDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(LocalDateTime.now());
        return deliveryRepository.save(delivery);
    }

    public List<Delivery> getCourierDeliveries(Long courierId) {
        courierRepository.findById(courierId)
                .orElseThrow(() -> new RuntimeException("Courier not found"));
        return deliveryRepository.findByCourierId(courierId);
    }
}
