package ru.mtuci.rbpo2025.dto;

import ru.mtuci.rbpo2025.model.DeliveryStatus;

public class SetDeliveryStatusRequest {
    private DeliveryStatus status;

    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
}
