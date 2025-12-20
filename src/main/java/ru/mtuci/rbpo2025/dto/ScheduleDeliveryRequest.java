package ru.mtuci.rbpo2025.dto;

import java.time.LocalDateTime;

public class ScheduleDeliveryRequest {
    private Long deliveryId;
    private LocalDateTime scheduledFor;

    public Long getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(Long deliveryId) {
        this.deliveryId = deliveryId;
    }

    public LocalDateTime getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(LocalDateTime scheduledFor) {
        this.scheduledFor = scheduledFor;
    }
}

