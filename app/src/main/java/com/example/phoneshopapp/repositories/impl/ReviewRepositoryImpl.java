package com.example.phoneshopapp.repositories.impl;

import android.util.Log;

import com.example.phoneshopapp.models.Review;
import com.example.phoneshopapp.repositories.ReviewRepository;
import com.example.phoneshopapp.repositories.callbacks.BooleanCallback;
import com.example.phoneshopapp.repositories.callbacks.ReviewCallback;
import com.example.phoneshopapp.repositories.callbacks.ReviewListCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firebase implementation of ReviewRepository
 * 
 * IMPORTANT NOTES:
 * - Collection names: "reviews", "orders", "PhoneDB"
 * - Review is PERMANENT (no update/delete methods)
 * - After creating review, automatically updates:
 *   1. Order.hasReview = true
 *   2. Order.reviewId = reviewId
 *   3. PhoneDB.averageRating and totalReviews
 */
public class ReviewRepositoryImpl implements ReviewRepository {
    private static final String TAG = "ReviewRepositoryImpl";
    private static final String COLLECTION_REVIEWS = "reviews";
    private static final String COLLECTION_ORDERS = "orders";
    private static final String COLLECTION_PHONES = "PhoneDB";  // ✅ Bảng sản phẩm chính

    private final FirebaseFirestore db;
    private final CollectionReference reviewsRef;
    private final CollectionReference ordersRef;
    private final CollectionReference phonesRef;

    public ReviewRepositoryImpl() {
        db = FirebaseFirestore.getInstance();
        reviewsRef = db.collection(COLLECTION_REVIEWS);
        ordersRef = db.collection(COLLECTION_ORDERS);
        phonesRef = db.collection(COLLECTION_PHONES);
    }

    /**
     * Tạo review MỚI
     * QUAN TRỌNG: Sau khi tạo review thành công, phải update:
     * 1. Order.hasReview = true
     * 2. Order.reviewId = reviewId
     * 3. PhoneDB.averageRating và totalReviews
     */
    @Override
    public void createReview(Review review, ReviewCallback callback) {
        try {
            // 1. Generate reviewId if not set
            if (review.getReviewId() == null || review.getReviewId().isEmpty()) {
                String reviewId = reviewsRef.document().getId();
                review.setReviewId(reviewId);
            }

            // 2. Set timestamps
            Date now = new Date();
            if (review.getCreatedAt() == null) {
                review.setCreatedAt(now);
            }
            if (review.getUpdatedAt() == null) {
                review.setUpdatedAt(now);
            }

            // 3. Set verified purchase (always true)
            review.setVerifiedPurchase(true);

            // 4. Convert to Map
            Map<String, Object> reviewData = reviewToMap(review);

            // 5. Save to Firestore
            reviewsRef.document(review.getReviewId())
                    .set(reviewData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Review created successfully: " + review.getReviewId());

                        // 6. Update Order hasReview = true
                        updateOrderHasReview(review.getOrderId(), review.getReviewId());

                        // 7. Update Product stats
                        updateProductStats(review.getProductId());

                        callback.onSuccess(review);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error creating review", e);
                        callback.onError("Lỗi tạo đánh giá: " + e.getMessage());
                    });

        } catch (Exception e) {
            Log.e(TAG, "Exception in createReview", e);
            callback.onError("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Update Order hasReview = true và reviewId
     * Helper method được gọi tự động sau khi tạo review
     */
    private void updateOrderHasReview(String orderId, String reviewId) {
        if (orderId == null || orderId.isEmpty()) {
            Log.w(TAG, "Cannot update order: orderId is null or empty");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("hasReview", true);
        updates.put("reviewId", reviewId);
        updates.put("updatedAt", new Date());

        ordersRef.document(orderId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Updated order hasReview = true for orderId: " + orderId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to update order hasReview for orderId: " + orderId, e);
                });
    }

    /**
     * Update Product averageRating và totalReviews
     * Helper method được gọi tự động sau khi tạo review
     * 
     * Logic:
     * 1. Query tất cả reviews của product
     * 2. Calculate average rating
     * 3. Update vào PhoneDB collection
     */
    private void updateProductStats(String productId) {
        if (productId == null || productId.isEmpty()) {
            Log.w(TAG, "Cannot update product stats: productId is null or empty");
            return;
        }

        // Query all reviews of this product
        reviewsRef.whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Review> reviews = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        try {
                            Review review = documentToReview(doc);
                            if (review != null) {
                                reviews.add(review);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error converting review document", e);
                        }
                    }

                    if (!reviews.isEmpty()) {
                        // Calculate average rating
                        float totalRating = 0;
                        for (Review review : reviews) {
                            totalRating += review.getRating();
                        }
                        float avgRating = totalRating / reviews.size();
                        int totalReviews = reviews.size();

                        // Update product in PhoneDB
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("averageRating", avgRating);
                        updates.put("totalReviews", totalReviews);

                        phonesRef.document(productId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, String.format("✅ Updated product stats: productId=%s, avgRating=%.1f, totalReviews=%d",
                                            productId, avgRating, totalReviews));
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "❌ Failed to update product stats for productId: " + productId, e);
                                });
                    } else {
                        Log.w(TAG, "No reviews found for productId: " + productId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying reviews for product stats", e);
                });
    }

    /**
     * Lấy tất cả reviews của 1 product
     * Query: WHERE productId == productId ORDER BY createdAt DESC
     */
    @Override
    public void getReviewsByProductId(String productId, ReviewListCallback callback) {
        Log.d(TAG, "========== QUERYING REVIEWS ==========");
        Log.d(TAG, "Searching for productId: " + productId);
        Log.d(TAG, "Collection: " + COLLECTION_REVIEWS);
        
        reviewsRef.whereEqualTo("productId", productId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "Query SUCCESS - Total documents returned: " + querySnapshot.size());
                    
                    List<Review> reviews = new ArrayList<>();
                    int docIndex = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        docIndex++;
                        try {
                            Log.d(TAG, "--- Document #" + docIndex + " ---");
                            Log.d(TAG, "Document ID: " + doc.getId());
                            Log.d(TAG, "Raw data: " + doc.getData());
                            
                            Review review = documentToReview(doc);
                            if (review != null) {
                                reviews.add(review);
                                Log.d(TAG, "✅ Converted successfully: " + review.getUserName() + " - " + review.getRating() + " stars");
                            } else {
                                Log.w(TAG, "❌ documentToReview returned null for doc: " + doc.getId());
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, "❌ Error converting review document: " + doc.getId(), ex);
                        }
                    }

                    Log.d(TAG, "========== QUERY COMPLETE ==========");
                    Log.d(TAG, "Total reviews converted: " + reviews.size() + " / " + querySnapshot.size());
                    callback.onSuccess(reviews);
                })
                .addOnFailureListener(e -> {
                    // Fallback if missing composite index for (productId + createdAt)
                    String msg = e != null ? String.valueOf(e.getMessage()) : "";
                    Log.e(TAG, "Error getting reviews by productId (with orderBy)", e);
                    if (msg.contains("FAILED_PRECONDITION") || msg.toLowerCase().contains("index")) {
                        Log.w(TAG, "Missing index detected. Falling back to query without orderBy(createdAt)...");
                        reviewsRef.whereEqualTo("productId", productId)
                                .get()
                                .addOnSuccessListener(qs -> {
                                    List<Review> reviews = new ArrayList<>();
                                    for (QueryDocumentSnapshot doc : qs) {
                                        try {
                                            Review review = documentToReview(doc);
                                            if (review != null) {
                                                reviews.add(review);
                                            }
                                        } catch (Exception ex) {
                                            Log.e(TAG, "Error converting review document", ex);
                                        }
                                    }
                                    // Sort in-memory by createdAt desc if available
                                    try {
                                        reviews.sort((a, b) -> {
                                            java.util.Date da = a.getCreatedAt();
                                            java.util.Date db = b.getCreatedAt();
                                            if (da == null && db == null) return 0;
                                            if (da == null) return 1;
                                            if (db == null) return -1;
                                            return db.compareTo(da);
                                        });
                                    } catch (Exception sortEx) {
                                        Log.w(TAG, "Failed to sort reviews in-memory", sortEx);
                                    }

                                    Log.d(TAG, "Retrieved (fallback) " + reviews.size() + " reviews for productId: " + productId);
                                    callback.onSuccess(reviews);
                                })
                                .addOnFailureListener(e2 -> {
                                    Log.e(TAG, "Fallback query without orderBy failed", e2);
                                    callback.onError("Lỗi tải đánh giá (fallback): " + e2.getMessage());
                                });
                    } else {
                        callback.onError("Lỗi tải đánh giá: " + msg);
                    }
                });
    }

    /**
     * Lấy tất cả reviews của user
     * Query: WHERE userId == userId ORDER BY createdAt DESC
     */
    @Override
    public void getUserReviews(String userId, ReviewListCallback callback) {
        reviewsRef.whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Review> reviews = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        try {
                            Review review = documentToReview(doc);
                            if (review != null) {
                                reviews.add(review);
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, "Error converting review document", ex);
                        }
                    }

                    Log.d(TAG, "Retrieved " + reviews.size() + " reviews for userId: " + userId);
                    callback.onSuccess(reviews);
                })
                .addOnFailureListener(e -> {
                    // Fallback if missing composite index for (userId + createdAt)
                    String msg = e != null ? String.valueOf(e.getMessage()) : "";
                    Log.e(TAG, "Error getting user reviews (with orderBy)", e);
                    if (msg.contains("FAILED_PRECONDITION") || msg.toLowerCase().contains("index")) {
                        Log.w(TAG, "Missing index detected. Falling back to query without orderBy(createdAt)...");
                        reviewsRef.whereEqualTo("userId", userId)
                                .get()
                                .addOnSuccessListener(qs -> {
                                    List<Review> reviews = new ArrayList<>();
                                    for (QueryDocumentSnapshot doc : qs) {
                                        try {
                                            Review review = documentToReview(doc);
                                            if (review != null) {
                                                reviews.add(review);
                                            }
                                        } catch (Exception ex) {
                                            Log.e(TAG, "Error converting review document", ex);
                                        }
                                    }
                                    // Sort in-memory by createdAt desc if available
                                    try {
                                        reviews.sort((a, b) -> {
                                            java.util.Date da = a.getCreatedAt();
                                            java.util.Date db = b.getCreatedAt();
                                            if (da == null && db == null) return 0;
                                            if (da == null) return 1;
                                            if (db == null) return -1;
                                            return db.compareTo(da);
                                        });
                                    } catch (Exception sortEx) {
                                        Log.w(TAG, "Failed to sort reviews in-memory", sortEx);
                                    }

                                    Log.d(TAG, "Retrieved (fallback) " + reviews.size() + " reviews for userId: " + userId);
                                    callback.onSuccess(reviews);
                                })
                                .addOnFailureListener(e2 -> {
                                    Log.e(TAG, "Fallback user reviews query without orderBy failed", e2);
                                    callback.onError("Lỗi tải đánh giá của bạn (fallback): " + e2.getMessage());
                                });
                    } else {
                        callback.onError("Lỗi tải đánh giá của bạn: " + msg);
                    }
                });
    }

    /**
     * Kiểm tra đơn hàng đã được đánh giá chưa
     * QUAN TRỌNG: Query theo orderId (không phải productId)
     * Query: WHERE orderId == orderId LIMIT 1
     * Return: true nếu tìm thấy review, false nếu không
     */
    @Override
    public void checkOrderHasReviewed(String orderId, BooleanCallback callback) {
        if (orderId == null || orderId.isEmpty()) {
            callback.onError("OrderId không hợp lệ");
            return;
        }

        reviewsRef.whereEqualTo("orderId", orderId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean hasReviewed = !querySnapshot.isEmpty();
                    Log.d(TAG, "Check order reviewed: orderId=" + orderId + ", hasReviewed=" + hasReviewed);
                    callback.onResult(hasReviewed);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking order reviewed status", e);
                    callback.onError("Lỗi kiểm tra đánh giá: " + e.getMessage());
                });
    }

    /**
     * ❌ KHÔNG IMPLEMENT updateReview() - review là permanent
     */

    /**
     * ❌ KHÔNG IMPLEMENT deleteReview() - review là permanent
     */

    // ==================== HELPER METHODS ====================

    /**
     * Convert Review object to Map for Firestore
     */
    private Map<String, Object> reviewToMap(Review review) {
        Map<String, Object> map = new HashMap<>();
        map.put("reviewId", review.getReviewId());
        map.put("orderId", review.getOrderId());
        map.put("userId", review.getUserId());
        map.put("userName", review.getUserName());
        map.put("productId", review.getProductId());
        map.put("productName", review.getProductName());
        map.put("variantId", review.getVariantId());
        map.put("variantName", review.getVariantName());
        map.put("variantColor", review.getVariantColor());
        map.put("variantRam", review.getVariantRam());
        map.put("variantStorage", review.getVariantStorage());
        map.put("rating", review.getRating());
        map.put("comment", review.getComment());
        map.put("reviewImages", review.getReviewImages());
        map.put("isVerifiedPurchase", review.isVerifiedPurchase());
        map.put("createdAt", review.getCreatedAt());
        map.put("updatedAt", review.getUpdatedAt());
        return map;
    }

    /**
     * Convert Firestore document to Review object
     */
    private Review documentToReview(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Log.w(TAG, "Document does not exist: " + doc.getId());
            return null;
        }

        try {
            Log.d(TAG, "Converting document: " + doc.getId());
            Log.d(TAG, "All fields in document: " + doc.getData());
            
            Review review = new Review();
            review.setReviewId(doc.getString("reviewId"));
            review.setOrderId(doc.getString("orderId"));
            review.setUserId(doc.getString("userId"));
            review.setUserName(doc.getString("userName"));
            review.setProductId(doc.getString("productId"));
            review.setProductName(doc.getString("productName"));
            review.setVariantId(doc.getString("variantId"));
            review.setVariantName(doc.getString("variantName"));
            review.setVariantColor(doc.getString("variantColor"));
            review.setVariantRam(doc.getString("variantRam"));
            review.setVariantStorage(doc.getString("variantStorage"));

            // Handle rating (could be Double or Float)
            Object ratingObj = doc.get("rating");
            if (ratingObj instanceof Number) {
                review.setRating(((Number) ratingObj).floatValue());
                Log.d(TAG, "Rating: " + review.getRating());
            } else {
                Log.w(TAG, "Rating field is missing or not a number");
            }

            review.setComment(doc.getString("comment"));
            Log.d(TAG, "Comment: " + (doc.getString("comment") != null ? doc.getString("comment").substring(0, Math.min(50, doc.getString("comment").length())) : "null"));

            // Handle reviewImages
            List<String> reviewImages = (List<String>) doc.get("reviewImages");
            review.setReviewImages(reviewImages);

            // Handle isVerifiedPurchase
            Boolean isVerified = doc.getBoolean("isVerifiedPurchase");
            review.setVerifiedPurchase(isVerified != null ? isVerified : true);

            // Handle dates
            review.setCreatedAt(doc.getDate("createdAt"));
            review.setUpdatedAt(doc.getDate("updatedAt"));

            Log.d(TAG, "✅ Successfully converted review: " + review.getUserName() + " - Rating: " + review.getRating());
            return review;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error converting document to Review: " + doc.getId(), e);
            Log.e(TAG, "Document data was: " + doc.getData());
            return null;
        }
    }
}
