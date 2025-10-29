package com.example.phoneshopapp.models;

public class OrderItem {
    private String productId;
    private String productName;
    private double price;
    private int quantity;
    private String imageUrl;
    private double totalPrice;

    // Variant information
    private String variantId;
    private String variantName;
    private String variantShortName;
    private String variantColor;
    private String variantColorHex;
    private String variantRam;
    private String variantStorage;

    public OrderItem() {
        // Default constructor required for calls to
        // DataSnapshot.getValue(OrderItem.class)
    }

    public OrderItem(String productId, String productName, double price, int quantity, String imageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.totalPrice = price * quantity;
    }

    // Getters and setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        this.totalPrice = price * quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = price * quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    // Variant getters and setters
    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public String getVariantShortName() {
        return variantShortName;
    }

    public void setVariantShortName(String variantShortName) {
        this.variantShortName = variantShortName;
    }

    public String getVariantColor() {
        return variantColor;
    }

    public void setVariantColor(String variantColor) {
        this.variantColor = variantColor;
    }

    public String getVariantColorHex() {
        return variantColorHex;
    }

    public void setVariantColorHex(String variantColorHex) {
        this.variantColorHex = variantColorHex;
    }

    public String getVariantRam() {
        return variantRam;
    }

    public void setVariantRam(String variantRam) {
        this.variantRam = variantRam;
    }

    public String getVariantStorage() {
        return variantStorage;
    }

    public void setVariantStorage(String variantStorage) {
        this.variantStorage = variantStorage;
    }

    // Helper methods
    public String getFormattedPrice() {
        return String.format("₫%.0f", price);
    }

    public String getFormattedTotalPrice() {
        return String.format("₫%.0f", totalPrice);
    }

    public void calculateTotalPrice() {
        this.totalPrice = price * quantity;
    }

    // Convert from CartItem
    public static OrderItem fromCartItem(CartItem cartItem) {
        OrderItem orderItem = new OrderItem(
                String.valueOf(cartItem.getProductId()),
                cartItem.getProductName(),
                cartItem.getProductPriceValue(),
                cartItem.getQuantity(),
                cartItem.getProductImageUrl());

        // Copy variant information from CartItem to OrderItem
        orderItem.setVariantId(cartItem.getVariantId());
        orderItem.setVariantName(cartItem.getVariantName());
        orderItem.setVariantShortName(cartItem.getVariantShortName());
        orderItem.setVariantColor(cartItem.getVariantColor());
        orderItem.setVariantColorHex(cartItem.getVariantColorHex());
        orderItem.setVariantRam(cartItem.getVariantRam());
        orderItem.setVariantStorage(cartItem.getVariantStorage());

        return orderItem;
    }
}