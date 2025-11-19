package com.example.phoneshopapp.data.favorite;

import android.util.Log;
import com.example.phoneshopapp.models.FavoriteItem;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for managing favorite items in Firestore
 * Handles all database operations for the favorites feature
 */
public class FavoriteRepository {
    private static final String TAG = "FavoriteRepository";
    private static final String COLLECTION_FAVORITES = "favorites";
    private final FirebaseFirestore db;

    public FavoriteRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Callback interfaces
    public interface OnFavoriteOperationListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnFavoritesLoadedListener {
        void onSuccess(List<FavoriteItem> favorites);
        void onFailure(Exception e);
    }

    public interface OnFavoriteCheckListener {
        void onResult(boolean isFavorite, String favoriteId);
        void onFailure(Exception e);
    }

    /**
     * Add a product to favorites
     * If product already exists in favorites, this will fail
     */
    public void addFavorite(String userId, FavoriteItem item, OnFavoriteOperationListener listener) {
        Log.d(TAG, "Adding favorite for user: " + userId + ", product: " + item.getProductName());

        // Check if already exists first
        isFavorite(userId, item.getProductId(), new OnFavoriteCheckListener() {
            @Override
            public void onResult(boolean isFavorite, String favoriteId) {
                if (isFavorite) {
                    Log.w(TAG, "Product already in favorites");
                    if (listener != null) {
                        listener.onFailure(new Exception("Sản phẩm đã có trong danh sách yêu thích"));
                    }
                } else {
                    // Add new favorite
                    addNewFavorite(item, listener);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.w(TAG, "Failed to check existing favorite, adding anyway", e);
                addNewFavorite(item, listener);
            }
        });
    }

    /**
     * Internal method to add new favorite item
     */
    private void addNewFavorite(FavoriteItem item, OnFavoriteOperationListener listener) {
        Map<String, Object> favoriteData = new HashMap<>();
        favoriteData.put("userId", item.getUserId());
        favoriteData.put("productId", item.getProductId());
        favoriteData.put("productName", item.getProductName());
        favoriteData.put("productPrice", item.getProductPrice());
        favoriteData.put("productPriceValue", item.getProductPriceValue());
        favoriteData.put("productImageUrl", item.getProductImageUrl());
        favoriteData.put("productCategory", item.getProductCategory());
        favoriteData.put("addedAt", item.getAddedAt());
        favoriteData.put("updatedAt", item.getUpdatedAt());

        db.collection(COLLECTION_FAVORITES)
                .add(favoriteData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Favorite added with ID: " + documentReference.getId());
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding favorite", e);
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    /**
     * Remove a favorite by document ID
     */
    public void removeFavorite(String favoriteId, OnFavoriteOperationListener listener) {
        Log.d(TAG, "Removing favorite: " + favoriteId);

        db.collection(COLLECTION_FAVORITES)
                .document(favoriteId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Favorite removed successfully");
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing favorite", e);
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    /**
     * Remove a favorite by userId and productId
     * Useful when you don't have the document ID
     */
    public void removeByProductId(String userId, String productId, OnFavoriteOperationListener listener) {
        Log.d(TAG, "Removing favorite by productId: " + productId);

        db.collection(COLLECTION_FAVORITES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("productId", productId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        removeFavorite(document.getId(), listener);
                    } else {
                        Log.w(TAG, "Favorite not found for productId: " + productId);
                        if (listener != null) {
                            listener.onFailure(new Exception("Không tìm thấy sản phẩm trong danh sách yêu thích"));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding favorite to remove", e);
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    /**
     * Get all favorites for a user
     * Sorted by addedAt in descending order (most recent first)
     * Note: Sorting done in-memory to avoid needing Firestore composite index
     */
    public void getFavorites(String userId, OnFavoritesLoadedListener listener) {
        Log.d(TAG, "Loading favorites for user: " + userId);

        db.collection(COLLECTION_FAVORITES)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<FavoriteItem> favorites = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        FavoriteItem item = documentToFavoriteItem(document);
                        if (item != null) {
                            favorites.add(item);
                        }
                    }
                    
                    // Sort in-memory by addedAt descending (most recent first)
                    favorites.sort((a, b) -> {
                        if (a.getAddedAt() == null && b.getAddedAt() == null) return 0;
                        if (a.getAddedAt() == null) return 1;
                        if (b.getAddedAt() == null) return -1;
                        return b.getAddedAt().compareTo(a.getAddedAt());
                    });
                    
                    Log.d(TAG, "Loaded " + favorites.size() + " favorites");
                    if (listener != null) {
                        listener.onSuccess(favorites);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading favorites", e);
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    /**
     * Check if a product is in user's favorites
     * Returns the favorite document ID if exists
     */
    public void isFavorite(String userId, String productId, OnFavoriteCheckListener listener) {
        db.collection(COLLECTION_FAVORITES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("productId", productId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        if (listener != null) {
                            listener.onResult(true, document.getId());
                        }
                    } else {
                        if (listener != null) {
                            listener.onResult(false, null);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking favorite status", e);
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    /**
     * Convert Firestore document to FavoriteItem
     */
    private FavoriteItem documentToFavoriteItem(DocumentSnapshot document) {
        try {
            FavoriteItem item = new FavoriteItem();
            item.setId(document.getId());
            item.setUserId(document.getString("userId"));
            item.setProductId(document.getString("productId"));
            item.setProductName(document.getString("productName"));
            item.setProductPrice(document.getString("productPrice"));
            
            // Handle numeric price
            Double priceValue = document.getDouble("productPriceValue");
            if (priceValue != null) {
                item.setProductPriceValue(priceValue);
            }
            
            item.setProductImageUrl(document.getString("productImageUrl"));
            item.setProductCategory(document.getString("productCategory"));
            
            // Handle dates
            Date addedAt = document.getDate("addedAt");
            if (addedAt != null) {
                item.setAddedAt(addedAt);
            }
            
            Date updatedAt = document.getDate("updatedAt");
            if (updatedAt != null) {
                item.setUpdatedAt(updatedAt);
            }
            
            return item;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to FavoriteItem", e);
            return null;
        }
    }

    /**
     * Get count of favorites for a user
     */
    public void getFavoriteCount(String userId, OnFavoriteCountListener listener) {
        db.collection(COLLECTION_FAVORITES)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (listener != null) {
                        listener.onSuccess(querySnapshot.size());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting favorite count", e);
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    public interface OnFavoriteCountListener {
        void onSuccess(int count);
        void onFailure(Exception e);
    }
}
