package com.example.phoneshopapp.models;

import java.util.Date;
import java.util.List;

public class Order {
    private String orderId;
    private String userId;
    private CustomerInfo customerInfo;
    private List<OrderItem> items;
    private PricingInfo pricing;
    private PaymentInfo paymentInfo;
    private OrderStatus orderStatus;
    private List<StatusHistory> statusHistory;
    private Date createdAt;
    private Date updatedAt;
    private Date estimatedDelivery;

    public Order() {
        // Default constructor required for calls to DataSnapshot.getValue(Order.class)
    }

    public Order(String orderId, String userId, CustomerInfo customerInfo, 
                List<OrderItem> items, PricingInfo pricing, PaymentInfo paymentInfo) {
        this.orderId = orderId;
        this.userId = userId;
        this.customerInfo = customerInfo;
        this.items = items;
        this.pricing = pricing;
        this.paymentInfo = paymentInfo;
        this.orderStatus = OrderStatus.PENDING;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters and setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public CustomerInfo getCustomerInfo() { return customerInfo; }
    public void setCustomerInfo(CustomerInfo customerInfo) { this.customerInfo = customerInfo; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public PricingInfo getPricing() { return pricing; }
    public void setPricing(PricingInfo pricing) { this.pricing = pricing; }

    public PaymentInfo getPaymentInfo() { return paymentInfo; }
    public void setPaymentInfo(PaymentInfo paymentInfo) { this.paymentInfo = paymentInfo; }

    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }

    public List<StatusHistory> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<StatusHistory> statusHistory) { this.statusHistory = statusHistory; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Date getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(Date estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }

    // Helper methods
    public String getFormattedOrderId() {
        return orderId != null ? orderId : "N/A";
    }

    public String getStatusDisplayName() {
        return orderStatus != null ? orderStatus.getDisplayName() : "N/A";
    }

    public double getTotalAmount() {
        return pricing != null ? pricing.getTotal() : 0.0;
    }

    public int getTotalItemCount() {
        if (items == null) return 0;
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }
}