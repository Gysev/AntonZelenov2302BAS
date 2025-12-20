package ru.mtuci.rbpo2025.dto;

public class ReassignCourierRequest {

    private Long newCourierId;

    public ReassignCourierRequest() {
    }

    public ReassignCourierRequest(Long newCourierId) {
        this.newCourierId = newCourierId;
    }

    public Long getNewCourierId() {
        return newCourierId;
    }

    public void setNewCourierId(Long newCourierId) {
        this.newCourierId = newCourierId;
    }
}
