package com.example.phoneshopapp.managers;

import android.content.Context;
import android.util.Log;

import com.example.phoneshopapp.models.Review;
import com.example.phoneshopapp.repositories.ReviewRepository;
import com.example.phoneshopapp.repositories.impl.ReviewRepositoryImpl;
import com.example.phoneshopapp.repositories.callbacks.BooleanCallback;
import com.example.phoneshopapp.repositories.callbacks.ReviewCallback;
import com.example.phoneshopapp.repositories.callbacks.ReviewListCallback;

import java.util.List;

/**
 * Singleton class quản lý business logic cho Review
 * Handles review validation, submission, and retrieval
 * 
 * IMPORTANT RULES:
 * - Review is PERMANENT (cannot be updated or deleted)
 * - One order can only be reviewed ONCE
 * - Review must be validated before submission
 */
public class ReviewManager {
    private static final String TAG = "ReviewManager";
    
    // Validation constants
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;
    private static final int MIN_COMMENT_LENGTH = 10;
    private static final int MAX_COMMENT_LENGTH = 500;
    private static final int MAX_REVIEW_IMAGES = 5;
    
    private static ReviewManager instance;
    private final ReviewRepository reviewRepository;
    private final Context context;

    private ReviewManager(Context context) {
        this.context = context.getApplicationContext();
        this.reviewRepository = new ReviewRepositoryImpl();
    }

    public static synchronized ReviewManager getInstance(Context context) {
        if (instance == null) {
            instance = new ReviewManager(context);
        }
        return instance;
    }

    /**
     * Validate và submit review
     * QUAN TRỌNG: Kiểm tra orderId chưa được review trước khi submit
     * 
     * Flow:
     * 1. Validate input (rating, comment, orderId)
     * 2. Check orderId chưa được review
     * 3. Submit review to repository
     * 
     * @param review Review object to submit
     * @param callback Callback for success/error
     */
    public void submitReview(Review review, ReviewCallback callback) {
        // 1. Validate input
        String validationError = validateReviewInput(review);
        if (validationError != null) {
            Log.w(TAG, "Review validation failed: " + validationError);
            callback.onError(validationError);
            return;
        }

        // 2. Kiểm tra đơn hàng đã review chưa
        String orderId = review.getOrderId();
        reviewRepository.checkOrderHasReviewed(orderId, new BooleanCallback() {
            @Override
            public void onResult(boolean hasReviewed) {
                if (hasReviewed) {
                    // Đã review rồi - không cho submit
                    Log.w(TAG, "Order already reviewed: " + orderId);
                    callback.onError("Đơn hàng này đã được đánh giá rồi");
                } else {
                    // Chưa review - cho phép submit
                    Log.d(TAG, "Submitting review for orderId: " + orderId);
                    reviewRepository.createReview(review, callback);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error checking order review status", new Exception(error));
                callback.onError("Lỗi kiểm tra đánh giá: " + error);
            }
        });
    }

    /**
     * Validate review input
     * Kiểm tra tất cả các điều kiện cần thiết trước khi submit
     * 
     * @param review Review object to validate
     * @return Error message nếu invalid, null nếu valid
     */
    private String validateReviewInput(Review review) {
        if (review == null) {
            return "Thông tin đánh giá không hợp lệ";
        }

        // Validate orderId
        if (review.getOrderId() == null || review.getOrderId().trim().isEmpty()) {
            return "Thiếu thông tin đơn hàng";
        }

        // Validate productId
        if (review.getProductId() == null || review.getProductId().trim().isEmpty()) {
            return "Thiếu thông tin sản phẩm";
        }

        // Validate userId
        if (review.getUserId() == null || review.getUserId().trim().isEmpty()) {
            return "Thiếu thông tin người dùng";
        }

        // Validate rating (1-5)
        if (review.getRating() < MIN_RATING || review.getRating() > MAX_RATING) {
            return "Vui lòng chọn số sao đánh giá (1-5)";
        }

        // Validate comment
        String comment = review.getComment();
        if (comment == null || comment.trim().isEmpty()) {
            return "Vui lòng nhập nhận xét";
        }

        String trimmedComment = comment.trim();
        if (trimmedComment.length() < MIN_COMMENT_LENGTH) {
            return String.format("Nhận xét quá ngắn (tối thiểu %d ký tự)", MIN_COMMENT_LENGTH);
        }

        if (trimmedComment.length() > MAX_COMMENT_LENGTH) {
            return String.format("Nhận xét quá dài (tối đa %d ký tự)", MAX_COMMENT_LENGTH);
        }

        // Validate review images (if any)
        if (review.getReviewImages() != null && review.getReviewImages().size() > MAX_REVIEW_IMAGES) {
            return String.format("Số lượng ảnh vượt quá giới hạn (tối đa %d ảnh)", MAX_REVIEW_IMAGES);
        }

        // All validations passed
        return null;
    }

    /**
     * Load reviews của sản phẩm
     * 
     * @param productId ID của sản phẩm (từ PhoneDB)
     * @param callback Callback với danh sách reviews
     */
    public void loadProductReviews(String productId, ReviewListCallback callback) {
        if (productId == null || productId.trim().isEmpty()) {
            callback.onError("ID sản phẩm không hợp lệ");
            return;
        }

        Log.d(TAG, "Loading reviews for productId: " + productId);
        reviewRepository.getReviewsByProductId(productId, callback);
    }

    /**
     * Load reviews của user hiện tại
     * 
     * @param userId ID của user
     * @param callback Callback với danh sách reviews
     */
    public void loadUserReviews(String userId, ReviewListCallback callback) {
        if (userId == null || userId.trim().isEmpty()) {
            callback.onError("ID người dùng không hợp lệ");
            return;
        }

        Log.d(TAG, "Loading reviews for userId: " + userId);
        reviewRepository.getUserReviews(userId, callback);
    }

    /**
     * Kiểm tra điều kiện đánh giá
     * Check xem đơn hàng đã được review chưa
     * 
     * @param orderId ID đơn hàng
     * @param callback Callback với kết quả (true = có thể review, false = đã review rồi)
     */
    public void checkCanReview(String orderId, BooleanCallback callback) {
        if (orderId == null || orderId.trim().isEmpty()) {
            callback.onError("Mã đơn hàng không hợp lệ");
            return;
        }

        // Query repository to check if order has been reviewed
        reviewRepository.checkOrderHasReviewed(orderId, new BooleanCallback() {
            @Override
            public void onResult(boolean hasReviewed) {
                // Invert the result: 
                // hasReviewed = true → canReview = false
                // hasReviewed = false → canReview = true
                boolean canReview = !hasReviewed;
                
                Log.d(TAG, String.format("Check canReview: orderId=%s, canReview=%b", orderId, canReview));
                callback.onResult(canReview);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error checking canReview for orderId: " + orderId, new Exception(error));
                callback.onError(error);
            }
        });
    }

    /**
     * Tính average rating từ danh sách reviews
     * Helper method cho UI
     * 
     * @param reviews Danh sách reviews
     * @return Average rating (0.0 nếu không có reviews)
     */
    public float calculateAverageRating(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0f;
        }

        float total = 0;
        for (Review review : reviews) {
            total += review.getRating();
        }

        return total / reviews.size();
    }

    /**
     * Đếm số lượng reviews theo rating
     * Helper method cho UI hiển thị rating distribution
     * 
     * @param reviews Danh sách reviews
     * @param rating Rating cần đếm (1-5)
     * @return Số lượng reviews có rating này
     */
    public int countReviewsByRating(List<Review> reviews, int rating) {
        if (reviews == null || reviews.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (Review review : reviews) {
            if (Math.round(review.getRating()) == rating) {
                count++;
            }
        }

        return count;
    }

    /**
     * Tính phần trăm reviews theo rating
     * Helper method cho UI hiển thị rating distribution
     * 
     * @param reviews Danh sách reviews
     * @param rating Rating cần tính (1-5)
     * @return Phần trăm (0-100)
     */
    public float calculateRatingPercentage(List<Review> reviews, int rating) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0f;
        }

        int count = countReviewsByRating(reviews, rating);
        return (count * 100.0f) / reviews.size();
    }

    /**
     * Filter reviews theo rating
     * Helper method cho UI filtering
     * 
     * @param reviews Danh sách reviews gốc
     * @param rating Rating cần filter (1-5), 0 = tất cả
     * @return Danh sách reviews đã filter
     */
    public List<Review> filterReviewsByRating(List<Review> reviews, int rating) {
        if (reviews == null || reviews.isEmpty() || rating == 0) {
            return reviews;
        }

        java.util.List<Review> filteredReviews = new java.util.ArrayList<>();
        for (Review review : reviews) {
            if (Math.round(review.getRating()) == rating) {
                filteredReviews.add(review);
            }
        }

        return filteredReviews;
    }

    /**
     * Filter reviews có verified purchase
     * Helper method cho UI filtering
     * 
     * @param reviews Danh sách reviews gốc
     * @return Danh sách reviews đã mua hàng
     */
    public List<Review> filterVerifiedPurchaseReviews(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return reviews;
        }

        java.util.List<Review> filteredReviews = new java.util.ArrayList<>();
        for (Review review : reviews) {
            if (review.isVerifiedPurchase()) {
                filteredReviews.add(review);
            }
        }

        return filteredReviews;
    }

    /**
     * Filter reviews có ảnh
     * Helper method cho UI filtering
     * 
     * @param reviews Danh sách reviews gốc
     * @return Danh sách reviews có ảnh
     */
    public List<Review> filterReviewsWithImages(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return reviews;
        }

        java.util.List<Review> filteredReviews = new java.util.ArrayList<>();
        for (Review review : reviews) {
            if (review.hasImages()) {
                filteredReviews.add(review);
            }
        }

        return filteredReviews;
    }

    // Getter methods for constants (useful for UI)
    
    public int getMinRating() {
        return MIN_RATING;
    }

    public int getMaxRating() {
        return MAX_RATING;
    }

    public int getMinCommentLength() {
        return MIN_COMMENT_LENGTH;
    }

    public int getMaxCommentLength() {
        return MAX_COMMENT_LENGTH;
    }

    public int getMaxReviewImages() {
        return MAX_REVIEW_IMAGES;
    }

    /**
     * ❌ KHÔNG CÓ updateReview() - review là permanent
     * Review không thể sửa sau khi tạo
     */

    /**
     * ❌ KHÔNG CÓ deleteReview() - review là permanent
     * Review không thể xóa sau khi tạo
     */
}
