package com.example.phoneshopapp.repositories.callbacks;

import com.example.phoneshopapp.models.Review;

/**
 * Callback for single Review operations
 */
public interface ReviewCallback {
    /**
     * Called when review operation succeeds
     * @param review The review object
     */
    void onSuccess(Review review);
    
    /**
     * Called when review operation fails
     * @param error Error message
     */
    void onError(String error);
}
