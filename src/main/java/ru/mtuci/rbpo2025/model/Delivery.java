package ru.mtuci.rbpo2025.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long parcelId;
    private Long courierId;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime deliveredAt;

    private LocalDate deliveryDate;
    private Integer slotNumber;
    
    private LocalDateTime scheduledFor;
    
    private LocalDateTime slaDeadline;
    private Boolean isOverdue;
    
    private Double cancellationCompensation;
    private String cancellationReason;
}
