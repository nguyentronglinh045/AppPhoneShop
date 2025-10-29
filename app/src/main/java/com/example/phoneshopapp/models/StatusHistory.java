package com.example.phoneshopapp.models;

import java.util.Date;

public class StatusHistory {
    private OrderStatus status;
    private Date timestamp;
    private String note;

    public StatusHistory() {
        // Default constructor required for calls to DataSnapshot.getValue(StatusHistory.class)
    }

    public StatusHistory(OrderStatus status, String note) {
        this.status = status;
        this.note = note;
        this.timestamp = new Date();
    }

    public StatusHistory(OrderStatus status, Date timestamp, String note) {
        this.status = status;
        this.timestamp = timestamp;
        this.note = note;
    }

    // Getters and setters
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    // Helper methods
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "N/A";
    }

    public String getFormattedTimestamp() {
        if (timestamp == null) return "N/A";
        return java.text.DateFormat.getDateTimeInstance().format(timestamp);
    }
}