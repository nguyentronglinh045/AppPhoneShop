package com.example.phoneshopapp.models;

import java.util.Date;

/**
 * Model class representing a favorite item in user's wishlist
 * Stores cached product information for quick display
 */
public class FavoriteItem {
    private String id;                    // Firestore document ID
    private String userId;                // Owner of this favorite
    private String productId;             // Reference to product
    private String productName;           // Cached product name
    private String productPrice;          // Cached price (formatted string)
    private double productPriceValue;     // Cached price (numeric value)
    private String productImageUrl;       // Cached image URL
    private String productCategory;       // Product category (Phone, Tablet, etc.)
    private Date addedAt;                 // When added to favorites
    private Date updatedAt;               // Last update time

    /**
     * Default constructor required for Firestore deserialization
     */
    public FavoriteItem() {
    }

    /**
     * Full constructor with all fields
     */
    public FavoriteItem(String userId, String productId, String productName, 
                       String productPrice, double productPriceValue, 
                       String productImageUrl, String productCategory) {
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productPriceValue = productPriceValue;
        this.productImageUrl = productImageUrl;
        this.productCategory = productCategory;
        this.addedAt = new Date();
        this.updatedAt = new Date();
    }

    /**
     * Constructor from Product object
     * Caches relevant product information for faster display
     */
    public FavoriteItem(String userId, com.example.phoneshopapp.Product product) {
        this.userId = userId;
        this.productId = product.getId();
        this.productName = product.getName();
        this.productPrice = product.getPrice();
        this.productPriceValue = product.getPriceValue();
        this.productImageUrl = product.getImageUrl();
        this.productCategory = product.getCategory();
        this.addedAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public double getProductPriceValue() {
        return productPriceValue;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public Date getAddedAt() {
        return addedAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public void setProductPriceValue(double productPriceValue) {
        this.productPriceValue = productPriceValue;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public void setAddedAt(Date addedAt) {
        this.addedAt = addedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        FavoriteItem that = (FavoriteItem) obj;
        return productId != null && productId.equals(that.productId) &&
               userId != null && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (productId != null ? productId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FavoriteItem{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", productPrice='" + productPrice + '\'' +
                '}';
    }
}
