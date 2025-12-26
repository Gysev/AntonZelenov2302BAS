package ru.mtuci.rbpo2025.dto;

import java.util.List;

public class RedistributeDeliveriesResponse {
    private Long unavailableCourierId;
    private int redistributedCount;
    private List<Long> redistributedDeliveryIds;
    private String message;

    public RedistributeDeliveriesResponse() {
    }

    public RedistributeDeliveriesResponse(Long unavailableCourierId, int redistributedCount, 
                                         List<Long> redistributedDeliveryIds, String message) {
        this.unavailableCourierId = unavailableCourierId;
        this.redistributedCount = redistributedCount;
        this.redistributedDeliveryIds = redistributedDeliveryIds;
        this.message = message;
    }

    public Long getUnavailableCourierId() {
        return unavailableCourierId;
    }

    public void setUnavailableCourierId(Long unavailableCourierId) {
        this.unavailableCourierId = unavailableCourierId;
    }

    public int getRedistributedCount() {
        return redistributedCount;
    }

    public void setRedistributedCount(int redistributedCount) {
        this.redistributedCount = redistributedCount;
    }

    public List<Long> getRedistributedDeliveryIds() {
        return redistributedDeliveryIds;
    }

    public void setRedistributedDeliveryIds(List<Long> redistributedDeliveryIds) {
        this.redistributedDeliveryIds = redistributedDeliveryIds;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}



