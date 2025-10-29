package com.example.phoneshopapp.models;

import java.util.Date;

public class PaymentInfo {
    private PaymentMethod method;
    private PaymentStatus status;
    private Date paidAt;
    private String transactionId; // For non-COD methods

    public PaymentInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(PaymentInfo.class)
    }

    public PaymentInfo(PaymentMethod method) {
        this.method = method;
        this.status = PaymentStatus.PENDING;
    }

    public PaymentInfo(PaymentMethod method, PaymentStatus status) {
        this.method = method;
        this.status = status;
    }

    // Getters and setters
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public Date getPaidAt() { return paidAt; }
    public void setPaidAt(Date paidAt) { this.paidAt = paidAt; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    // Helper methods
    public String getMethodDisplayName() {
        return method != null ? method.getDisplayName() : "N/A";
    }

    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "N/A";
    }

    public boolean isPaid() {
        return status == PaymentStatus.PAID;
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }

    public boolean isCOD() {
        return method == PaymentMethod.COD;
    }

    public void markAsPaid() {
        this.status = PaymentStatus.PAID;
        this.paidAt = new Date();
    }

    public void markAsFailed() {
        this.status = PaymentStatus.FAILED;
    }
}