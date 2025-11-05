package com.example.phoneshopapp.model;

import java.util.Date;

public class Review {
    private String id;
    private String userId;
    private String userName;
    private String productId;
    private float rating;
    private String comment;
    private Date date;
    private String avatarUrl;
    
    // Variant info
    private String variantName;
    private String variantColor;
    private String variantRam;
    private String variantStorage;
    
    // Metadata
    private boolean isVerifiedPurchase;

    public Review() {
        // Empty constructor required for Firebase
    }

    public Review(String userId, String userName, String productId, float rating, String comment) {
        this.userId = userId;
        this.userName = userName;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
        this.date = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getVariantName() {
        return variantName;
    }
    
    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }
    
    public String getVariantColor() {
        return variantColor;
    }
    
    public void setVariantColor(String variantColor) {
        this.variantColor = variantColor;
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
    
    public boolean isVerifiedPurchase() {
        return isVerifiedPurchase;
    }
    
    public void setVerifiedPurchase(boolean verifiedPurchase) {
        isVerifiedPurchase = verifiedPurchase;
    }
    
    /**
     * Get formatted variant info for display
     * Example: "Titan Tự nhiên - 8GB/256GB"
     */
    public String getFormattedVariantInfo() {
        if (variantName != null && !variantName.isEmpty()) {
            return variantName;
        }
        
        StringBuilder sb = new StringBuilder();
        if (variantColor != null && !variantColor.isEmpty()) {
            sb.append(variantColor);
        }
        if (variantRam != null && variantStorage != null) {
            if (sb.length() > 0) {
                sb.append(" - ");
            }
            sb.append(variantRam).append("/").append(variantStorage);
        }
        
        return sb.length() > 0 ? sb.toString() : null;
    }
}