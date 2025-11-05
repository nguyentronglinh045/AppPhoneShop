package com.example.phoneshopapp.repositories.callbacks;

/**
 * Callback for boolean result operations
 * Used for checking conditions like hasReviewed, canReview, etc.
 */
public interface BooleanCallback {
    /**
     * Called when check operation succeeds
     * @param result The boolean result
     */
    void onResult(boolean result);
    
    /**
     * Called when check operation fails
     * @param error Error message
     */
    void onError(String error);
}
