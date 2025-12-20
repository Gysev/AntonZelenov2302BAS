package ru.mtuci.rbpo2025.dto;

import java.util.List;

public class CheckSlaResponse {
    private int checkedCount;
    private int overdueCount;
    private List<Long> overdueDeliveryIds;
    private String message;

    public CheckSlaResponse() {
    }

    public CheckSlaResponse(int checkedCount, int overdueCount, List<Long> overdueDeliveryIds, String message) {
        this.checkedCount = checkedCount;
        this.overdueCount = overdueCount;
        this.overdueDeliveryIds = overdueDeliveryIds;
        this.message = message;
    }

    public int getCheckedCount() {
        return checkedCount;
    }

    public void setCheckedCount(int checkedCount) {
        this.checkedCount = checkedCount;
    }

    public int getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(int overdueCount) {
        this.overdueCount = overdueCount;
    }

    public List<Long> getOverdueDeliveryIds() {
        return overdueDeliveryIds;
    }

    public void setOverdueDeliveryIds(List<Long> overdueDeliveryIds) {
        this.overdueDeliveryIds = overdueDeliveryIds;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}


