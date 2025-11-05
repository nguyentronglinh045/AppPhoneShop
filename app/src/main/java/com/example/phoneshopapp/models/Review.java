package com.example.phoneshopapp.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Review {
    private String reviewId;
    private String orderId;           // ID đơn hàng
    private String userId;
    private String userName;
    private String productId;
    private String productName;

    // Thông tin variant đã mua
    private String variantId;
    private String variantName;       // Ví dụ: "Titan Tự nhiên - 8GB/256GB"
    private String variantColor;
    private String variantRam;
    private String variantStorage;

    // Nội dung đánh giá
    private float rating;             // 1.0 - 5.0
    private String comment;
    private List<String> reviewImages; // URLs ảnh đánh giá (optional)

    // Metadata
    private Date createdAt;
    private Date updatedAt;
    private boolean isVerifiedPurchase; // Luôn true vì phải mua mới đánh giá được

    // Legacy field for compatibility (if needed)
    private String avatarUrl;

    // Default constructor required for Firebase
    public Review() {
    }

    // Full constructor
    public Review(String reviewId, String orderId, String userId, String userName, 
                  String productId, String productName, String variantId, String variantName,
                  String variantColor, String variantRam, String variantStorage,
                  float rating, String comment, List<String> reviewImages,
                  Date createdAt, Date updatedAt, boolean isVerifiedPurchase) {
        this.reviewId = reviewId;
        this.orderId = orderId;
        this.userId = userId;
        this.userName = userName;
        this.productId = productId;
        this.productName = productName;
        this.variantId = variantId;
        this.variantName = variantName;
        this.variantColor = variantColor;
        this.variantRam = variantRam;
        this.variantStorage = variantStorage;
        this.rating = rating;
        this.comment = comment;
        this.reviewImages = reviewImages;
        this.createdAt = createdAt != null ? createdAt : new Date();
        this.updatedAt = updatedAt != null ? updatedAt : new Date();
        this.isVerifiedPurchase = isVerifiedPurchase;
    }

    // Simple constructor
    public Review(String orderId, String userId, String userName, String productId, 
                  float rating, String comment) {
        this.orderId = orderId;
        this.userId = userId;
        this.userName = userName;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isVerifiedPurchase = true;
    }

    // Getters and Setters
    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

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

    public List<String> getReviewImages() {
        return reviewImages;
    }

    public void setReviewImages(List<String> reviewImages) {
        this.reviewImages = reviewImages;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isVerifiedPurchase() {
        return isVerifiedPurchase;
    }

    public void setVerifiedPurchase(boolean verifiedPurchase) {
        isVerifiedPurchase = verifiedPurchase;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    // Legacy getter for compatibility
    public Date getDate() {
        return createdAt;
    }

    public void setDate(Date date) {
        this.createdAt = date;
    }

    // Legacy getter for compatibility
    public String getId() {
        return reviewId;
    }

    public void setId(String id) {
        this.reviewId = id;
    }

    // Helper Methods
    /**
     * Lấy thông tin variant đã format
     * @return String như "Titan Tự nhiên - 8GB/256GB"
     */
    public String getFormattedVariant() {
        if (variantName != null && !variantName.isEmpty()) {
            return variantName;
        }
        
        StringBuilder sb = new StringBuilder();
        if (variantColor != null && !variantColor.isEmpty()) {
            sb.append(variantColor);
        }
        if (variantRam != null && !variantRam.isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(variantRam);
        }
        if (variantStorage != null && !variantStorage.isEmpty()) {
            if (sb.length() > 0) sb.append("/");
            sb.append(variantStorage);
        }
        
        return sb.length() > 0 ? sb.toString() : "";
    }

    /**
     * Lấy ngày đánh giá đã format
     * @return String như "01/11/2024"
     */
    public String getFormattedDate() {
        if (createdAt == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(createdAt);
    }

    /**
     * Lấy rating đã format
     * @return String như "4.5"
     */
    public String getFormattedRating() {
        return String.format(Locale.getDefault(), "%.1f", rating);
    }

    /**
     * Kiểm tra có ảnh đánh giá không
     * @return true nếu có ảnh
     */
    public boolean hasImages() {
        return reviewImages != null && !reviewImages.isEmpty();
    }

    /**
     * Lấy số lượng ảnh
     * @return số lượng ảnh
     */
    public int getImageCount() {
        return reviewImages != null ? reviewImages.size() : 0;
    }
}
