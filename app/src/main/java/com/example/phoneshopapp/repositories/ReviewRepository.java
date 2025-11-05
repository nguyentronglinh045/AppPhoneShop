package com.example.phoneshopapp.repositories;

import com.example.phoneshopapp.models.Review;
import com.example.phoneshopapp.repositories.callbacks.ReviewCallback;
import com.example.phoneshopapp.repositories.callbacks.ReviewListCallback;
import com.example.phoneshopapp.repositories.callbacks.BooleanCallback;

/**
 * Repository interface for Review operations
 * 
 * IMPORTANT NOTES:
 * - Review is PERMANENT (no update/delete methods)
 * - One order can only be reviewed ONCE (check by orderId)
 * - After creating review, must update Order.hasReview and PhoneDB stats
 */
public interface ReviewRepository {

    /**
     * Tạo đánh giá mới (chỉ tạo 1 lần, không sửa/xóa được)
     * QUAN TRỌNG: 
     * - Sau khi tạo review thành công, tự động update:
     *   1. Order.hasReview = true
     *   2. Order.reviewId = reviewId
     *   3. PhoneDB.averageRating và totalReviews
     * 
     * @param review Review object to create
     * @param callback Callback for success/error
     */
    void createReview(Review review, ReviewCallback callback);

    /**
     * Lấy tất cả đánh giá của 1 sản phẩm
     * Query: WHERE productId == productId ORDER BY createdAt DESC
     * 
     * @param productId ID của sản phẩm (từ PhoneDB)
     * @param callback Callback with list of reviews
     */
    void getReviewsByProductId(String productId, ReviewListCallback callback);

    /**
     * Lấy tất cả đánh giá của user
     * Query: WHERE userId == userId ORDER BY createdAt DESC
     * 
     * @param userId ID của user
     * @param callback Callback with list of reviews
     */
    void getUserReviews(String userId, ReviewListCallback callback);

    /**
     * Kiểm tra user đã đánh giá đơn hàng này chưa
     * QUAN TRỌNG: Kiểm tra theo orderId (không phải productId)
     * Vì mỗi đơn hàng chỉ được đánh giá 1 lần
     * 
     * Query: WHERE orderId == orderId LIMIT 1
     * 
     * @param orderId ID của đơn hàng
     * @param callback Callback with result (true = đã review, false = chưa review)
     */
    void checkOrderHasReviewed(String orderId, BooleanCallback callback);

    /**
     * ❌ KHÔNG CÓ updateReview() - review là permanent
     * Review không thể sửa sau khi tạo
     */

    /**
     * ❌ KHÔNG CÓ deleteReview() - review là permanent
     * Review không thể xóa sau khi tạo
     */
}
