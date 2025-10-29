package com.example.phoneshopapp.models;

public class PricingInfo {
    private double subtotal;
    private double shippingFee;
    private double discount;
    private double total;

    public PricingInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(PricingInfo.class)
    }

    public PricingInfo(double subtotal, double shippingFee, double discount) {
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.discount = discount;
        this.total = subtotal + shippingFee - discount;
    }

    // Getters and setters
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { 
        this.subtotal = subtotal;
        calculateTotal();
    }

    public double getShippingFee() { return shippingFee; }
    public void setShippingFee(double shippingFee) { 
        this.shippingFee = shippingFee;
        calculateTotal();
    }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { 
        this.discount = discount;
        calculateTotal();
    }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    // Helper methods
    public void calculateTotal() {
        this.total = subtotal + shippingFee - discount;
    }

    public String getFormattedSubtotal() {
        return String.format("₫%.0f", subtotal);
    }

    public String getFormattedShippingFee() {
        return String.format("₫%.0f", shippingFee);
    }

    public String getFormattedDiscount() {
        return String.format("₫%.0f", discount);
    }

    public String getFormattedTotal() {
        return String.format("₫%.0f", total);
    }

    public boolean hasDiscount() {
        return discount > 0;
    }

    public boolean isFreeShipping() {
        return shippingFee <= 0;
    }
}