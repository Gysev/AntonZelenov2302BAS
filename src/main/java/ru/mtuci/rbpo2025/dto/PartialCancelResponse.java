package ru.mtuci.rbpo2025.dto;

public class PartialCancelResponse {
    private Long orderId;
    private String status;
    private Double compensationAmount;
    private String message;

    public PartialCancelResponse() {
    }

    public PartialCancelResponse(Long orderId, String status, Double compensationAmount, String message) {
        this.orderId = orderId;
        this.status = status;
        this.compensationAmount = compensationAmount;
        this.message = message;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getCompensationAmount() {
        return compensationAmount;
    }

    public void setCompensationAmount(Double compensationAmount) {
        this.compensationAmount = compensationAmount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}


