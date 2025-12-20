package ru.mtuci.rbpo2025.dto;

public class RedistributeDeliveriesRequest {
    private Long unavailableCourierId;
    private String reason;

    public Long getUnavailableCourierId() {
        return unavailableCourierId;
    }

    public void setUnavailableCourierId(Long unavailableCourierId) {
        this.unavailableCourierId = unavailableCourierId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

