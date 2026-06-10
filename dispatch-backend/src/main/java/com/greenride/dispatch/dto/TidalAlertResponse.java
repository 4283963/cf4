package com.greenride.dispatch.dto;

public class TidalAlertResponse {

    private String alertId;
    private boolean accepted;
    private String message;
    private String assignedCourierName;
    private Long dispatchOrderId;

    public TidalAlertResponse() {}

    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }

    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAssignedCourierName() { return assignedCourierName; }
    public void setAssignedCourierName(String assignedCourierName) { this.assignedCourierName = assignedCourierName; }

    public Long getDispatchOrderId() { return dispatchOrderId; }
    public void setDispatchOrderId(Long dispatchOrderId) { this.dispatchOrderId = dispatchOrderId; }
}
