package com.example.phoneshopapp.repositories.callbacks;

import com.example.phoneshopapp.models.Review;
import java.util.List;

/**
 * Callback for list of Reviews operations
 */
public interface ReviewListCallback {
    /**
     * Called when reviews are successfully loaded
     * @param reviews List of reviews
     */
    void onSuccess(List<Review> reviews);
    
    /**
     * Called when loading reviews fails
     * @param error Error message
     */
    void onError(String error);
}
