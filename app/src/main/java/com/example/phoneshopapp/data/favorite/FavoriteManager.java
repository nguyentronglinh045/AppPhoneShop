package com.example.phoneshopapp.data.favorite;

import android.content.Context;
import android.util.Log;
import com.example.phoneshopapp.Product;
import com.example.phoneshopapp.UserManager;
import com.example.phoneshopapp.models.FavoriteItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton manager for handling favorite operations
 * Provides centralized state management and automatic UI updates via listeners
 */
public class FavoriteManager {
    private static final String TAG = "FavoriteManager";
    private static FavoriteManager instance;
    private FavoriteRepository repository;
    private UserManager userManager;
    private List<FavoriteItem> favoriteItems;
    private List<FavoriteUpdateListener> listeners;

    // Listener interface for UI updates
    public interface FavoriteUpdateListener {
        void onFavoritesUpdated(List<FavoriteItem> favorites);
        void onFavoriteCountChanged(int count);
        void onFavoriteError(String message);
    }

    // Callback interface for operations
    public interface OnFavoriteOperationListener {
        void onSuccess(String message);
        void onFailure(String error);
    }

    private FavoriteManager() {
        this.repository = new FavoriteRepository();
        this.favoriteItems = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    /**
     * Get singleton instance
     */
    public static synchronized FavoriteManager getInstance() {
        if (instance == null) {
            instance = new FavoriteManager();
        }
        return instance;
    }

    /**
     * Initialize manager with context
     * Must be called before using any methods
     */
    public void initialize(Context context) {
        this.userManager = UserManager.getInstance(context);
        loadFavorites();
    }

    /**
     * Get repository instance for direct access
     */
    public FavoriteRepository getRepository() {
        return repository;
    }

    // ============================================
    // LISTENER MANAGEMENT
    // ============================================

    /**
     * Add a listener to receive updates
     */
    public void addListener(FavoriteUpdateListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener
     */
    public void removeListener(FavoriteUpdateListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners of updates
     */
    private void notifyFavoritesUpdated() {
        for (FavoriteUpdateListener listener : listeners) {
            listener.onFavoritesUpdated(new ArrayList<>(favoriteItems));
            listener.onFavoriteCountChanged(getFavoriteCount());
        }
    }

    /**
     * Notify all listeners of errors
     */
    private void notifyError(String message) {
        for (FavoriteUpdateListener listener : listeners) {
            listener.onFavoriteError(message);
        }
    }

    // ============================================
    // CORE FAVORITE OPERATIONS
    // ============================================

    /**
     * Load all favorites from Firestore
     */
    public void loadFavorites() {
        if (userManager == null) {
            Log.w(TAG, "UserManager is null, cannot load favorites");
            notifyError("Lỗi hệ thống: UserManager chưa được khởi tạo");
            return;
        }

        if (!userManager.isLoggedIn()) {
            Log.w(TAG, "User not logged in, cannot load favorites");
            favoriteItems.clear();
            notifyFavoritesUpdated();
            return;
        }

        String userId = userManager.getCurrentUserId();
        if (userId == null) {
            Log.w(TAG, "User ID is null, cannot load favorites");
            notifyError("Không thể xác định người dùng. Vui lòng đăng nhập lại.");
            return;
        }

        Log.d(TAG, "Loading favorites for user: " + userId);

        repository.getFavorites(userId, new FavoriteRepository.OnFavoritesLoadedListener() {
            @Override
            public void onSuccess(List<FavoriteItem> items) {
                favoriteItems.clear();
                favoriteItems.addAll(items);
                notifyFavoritesUpdated();
                Log.d(TAG, "Loaded " + items.size() + " favorites for user: " + userId);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load favorites for user: " + userId, e);
                notifyError("Không thể tải danh sách yêu thích: " + e.getMessage());
            }
        });
    }

    /**
     * Add a product to favorites
     */
    public void addFavorite(Product product, OnFavoriteOperationListener listener) {
        if (userManager == null || !userManager.isLoggedIn()) {
            Log.e(TAG, "User not logged in");
            if (listener != null) {
                listener.onFailure("Vui lòng đăng nhập để thêm sản phẩm yêu thích");
            }
            return;
        }

        String userId = userManager.getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "User ID is null");
            if (listener != null) {
                listener.onFailure("Không thể xác định người dùng");
            }
            return;
        }

        Log.d(TAG, "Adding product to favorites: " + product.getName());
        FavoriteItem favoriteItem = new FavoriteItem(userId, product);

        repository.addFavorite(userId, favoriteItem, new FavoriteRepository.OnFavoriteOperationListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Successfully added to favorites");
                loadFavorites(); // Reload to update UI
                if (listener != null) {
                    listener.onSuccess("Đã thêm vào yêu thích");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to add to favorites", e);
                if (listener != null) {
                    listener.onFailure("Không thể thêm vào yêu thích: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Remove a favorite by document ID
     */
    public void removeFavorite(String favoriteId, OnFavoriteOperationListener listener) {
        repository.removeFavorite(favoriteId, new FavoriteRepository.OnFavoriteOperationListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Successfully removed from favorites");
                loadFavorites(); // Reload to update UI
                if (listener != null) {
                    listener.onSuccess("Đã xóa khỏi yêu thích");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to remove from favorites", e);
                if (listener != null) {
                    listener.onFailure("Không thể xóa khỏi yêu thích: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Remove a favorite by product ID
     */
    public void removeFavoriteByProductId(String productId, OnFavoriteOperationListener listener) {
        if (userManager == null || !userManager.isLoggedIn()) {
            if (listener != null) {
                listener.onFailure("Người dùng chưa đăng nhập");
            }
            return;
        }

        String userId = userManager.getCurrentUserId();
        repository.removeByProductId(userId, productId, new FavoriteRepository.OnFavoriteOperationListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Successfully removed from favorites");
                loadFavorites(); // Reload to update UI
                if (listener != null) {
                    listener.onSuccess("Đã xóa khỏi yêu thích");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to remove from favorites", e);
                if (listener != null) {
                    listener.onFailure("Không thể xóa khỏi yêu thích: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Toggle favorite status of a product
     * Adds if not favorited, removes if already favorited
     * Uses Firestore check instead of memory to ensure accuracy
     */
    public void toggleFavorite(Product product, OnFavoriteOperationListener listener) {
        if (userManager == null || !userManager.isLoggedIn()) {
            if (listener != null) {
                listener.onFailure("Vui lòng đăng nhập");
            }
            return;
        }

        String userId = userManager.getCurrentUserId();
        if (userId == null) {
            if (listener != null) {
                listener.onFailure("Không thể xác định người dùng");
            }
            return;
        }

        // Check from Firestore to ensure accuracy
        repository.isFavorite(userId, product.getId(), new FavoriteRepository.OnFavoriteCheckListener() {
            @Override
            public void onResult(boolean isFavorite, String favoriteId) {
                if (isFavorite) {
                    // Remove from favorites using the favoriteId
                    repository.removeFavorite(favoriteId, new FavoriteRepository.OnFavoriteOperationListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Successfully removed from favorites");
                            loadFavorites(); // Reload to update UI
                            if (listener != null) {
                                listener.onSuccess("Đã xóa khỏi yêu thích");
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Failed to remove from favorites", e);
                            if (listener != null) {
                                listener.onFailure("Không thể xóa: " + e.getMessage());
                            }
                        }
                    });
                } else {
                    // Add to favorites
                    FavoriteItem favoriteItem = new FavoriteItem(userId, product);
                    repository.addFavorite(userId, favoriteItem, new FavoriteRepository.OnFavoriteOperationListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Successfully added to favorites");
                            loadFavorites(); // Reload to update UI
                            if (listener != null) {
                                listener.onSuccess("Đã thêm vào yêu thích");
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Failed to add to favorites", e);
                            if (listener != null) {
                                listener.onFailure("Không thể thêm: " + e.getMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to check favorite status", e);
                if (listener != null) {
                    listener.onFailure("Không thể kiểm tra trạng thái: " + e.getMessage());
                }
            }
        });
    }

    // ============================================
    // QUERY METHODS
    // ============================================

    /**
     * Check if a product is in favorites
     */
    public boolean isFavorite(String productId) {
        if (productId == null) return false;
        
        for (FavoriteItem item : favoriteItems) {
            if (productId.equals(item.getProductId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get favorite item by product ID
     */
    public FavoriteItem getFavoriteByProductId(String productId) {
        if (productId == null) return null;
        
        for (FavoriteItem item : favoriteItems) {
            if (productId.equals(item.getProductId())) {
                return item;
            }
        }
        return null;
    }

    /**
     * Get all favorites
     */
    public List<FavoriteItem> getFavorites() {
        return new ArrayList<>(favoriteItems);
    }

    /**
     * Get favorite count
     */
    public int getFavoriteCount() {
        return favoriteItems.size();
    }

    /**
     * Check if favorites list is empty
     */
    public boolean isEmpty() {
        return favoriteItems.isEmpty();
    }

    /**
     * Refresh favorites from server
     */
    public void refreshFavorites() {
        loadFavorites();
    }
}
