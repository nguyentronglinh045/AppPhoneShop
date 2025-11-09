package com.example.phoneshopapp.ui.home;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phoneshopapp.Category;
import com.example.phoneshopapp.Product;
import com.example.phoneshopapp.ProductManager;
import com.example.phoneshopapp.R;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private static final String TAG = "HomeViewModel";

    private final MutableLiveData<List<Category>> categories;
    private final MutableLiveData<List<Product>> popularProducts;
    private final MutableLiveData<List<Product>> bestDeals;
    private final MutableLiveData<List<Product>> flashSaleProducts;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    private final ProductManager productManager;

    // Static cache để giữ data giữa các lần navigation
    private static List<Product> cachedPopularProducts = null;
    private static List<Product> cachedBestDeals = null;
    private static List<Product> cachedFlashSaleProducts = null;
    private static long lastCacheTime = 0;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 phút cache

    public HomeViewModel() {
        categories = new MutableLiveData<>();
        popularProducts = new MutableLiveData<>();
        bestDeals = new MutableLiveData<>();
        flashSaleProducts = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        productManager = ProductManager.getInstance();

        loadCategories();

        // Luôn load từ Firebase để đảm bảo data mới nhất
        Log.d(TAG, "Force loading fresh data from Firebase on init");
        forceRefreshFromFirebase();
    }

    private void loadCategories() {
        // Load categories dynamically from products
        productManager.loadProductsFromFirebase(new ProductManager.OnProductsLoadedListener() {
            @Override
            public void onSuccess(List<Product> products) {
                // Extract unique categories from products
                List<Category> categoryList = new ArrayList<>();
                List<String> uniqueCategories = new ArrayList<>();
                
                for (Product product : products) {
                    String category = product.getCategory();
                    if (category != null && !category.isEmpty() && !uniqueCategories.contains(category)) {
                        uniqueCategories.add(category);
                        // Map category names to appropriate icons
                        int iconRes = getCategoryIcon(category);
                        categoryList.add(new Category(category, iconRes));
                    }
                }
                
                Log.d(TAG, "📂 Loaded " + categoryList.size() + " categories from products");
                categories.setValue(categoryList);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load categories: " + e.getMessage());
                // Fallback to empty list
                categories.setValue(new ArrayList<>());
            }
        });
    }
    
    /**
     * Map category names to appropriate icons
     */
    private int getCategoryIcon(String category) {
        if (category == null) return R.drawable.ic_home_black_24dp;
        
        String lowerCategory = category.toLowerCase();
        
        if (lowerCategory.contains("phone") || lowerCategory.contains("smartphone")) {
            return R.drawable.ic_home_black_24dp;
        } else if (lowerCategory.contains("tablet") || lowerCategory.contains("ipad")) {
            return R.drawable.ic_dashboard_black_24dp;
        } else if (lowerCategory.contains("accessory") || lowerCategory.contains("accessories")) {
            return R.drawable.ic_notifications_black_24dp;
        } else if (lowerCategory.contains("watch")) {
            return R.drawable.ic_home_black_24dp;
        } else if (lowerCategory.contains("audio") || lowerCategory.contains("headphone")) {
            return R.drawable.ic_dashboard_black_24dp;
        } else {
            return R.drawable.ic_notifications_black_24dp; // Default icon
        }
    }

    // Kiểm tra cache có còn valid không
    private boolean isCacheValid() {
        return cachedPopularProducts != null &&
                cachedBestDeals != null &&
                cachedFlashSaleProducts != null &&
                (System.currentTimeMillis() - lastCacheTime) < CACHE_DURATION;
    }

    // Load data từ cache
    private void loadFromCache() {
        isLoading.setValue(false);
        popularProducts.setValue(new ArrayList<>(cachedPopularProducts));
        bestDeals.setValue(new ArrayList<>(cachedBestDeals));
        flashSaleProducts.setValue(new ArrayList<>(cachedFlashSaleProducts));
        Log.d(TAG, "Loaded data from cache - Popular: " + cachedPopularProducts.size() +
                ", Deals: " + cachedBestDeals.size() +
                ", FlashSale: " + cachedFlashSaleProducts.size());
    }

    // Update cache với data mới
    private void updateCache(List<Product> featured, List<Product> deals, List<Product> flashSale) {
        cachedPopularProducts = new ArrayList<>(featured);
        cachedBestDeals = new ArrayList<>(deals);
        cachedFlashSaleProducts = new ArrayList<>(flashSale);
        lastCacheTime = System.currentTimeMillis();
        Log.d(TAG, "Cache updated with " + featured.size() + " featured, " + deals.size() + " deals, and " + flashSale.size() + " flash sale products");
    }

    // Clear cache (có thể gọi khi cần refresh)
    public static void clearCache() {
        cachedPopularProducts = null;
        cachedBestDeals = null;
        cachedFlashSaleProducts = null;
        lastCacheTime = 0;
        Log.d("HomeViewModel", "Cache cleared");
    }

    private void loadProductsFromFirebase() {
        Log.d(TAG, "Loading products from Firebase...");
        isLoading.setValue(true);
        errorMessage.setValue(null);

        productManager.loadProductsFromFirebase(new ProductManager.OnProductsLoadedListener() {
            @Override
            public void onSuccess(List<Product> products) {
                Log.d(TAG, "Successfully loaded " + products.size() + " products from Firebase");
                isLoading.setValue(false);

                List<Product> featured = new ArrayList<>();
                List<Product> deals = new ArrayList<>();
                List<Product> flashSale = new ArrayList<>();

                for (Product product : products) {
                    Log.d(TAG, "Product: " + product.getName() +
                            " | Featured: " + product.isFeatured() +
                            " | BestDeal: " + product.isBestDeal() +
                            " | FlashSale: " + product.isFlashSale() +
                            " | ImageUrl: " + product.getImageUrl());

                    if (product.isFeatured()) {
                        featured.add(product);
                    }
                    if (product.isBestDeal()) {
                        deals.add(product);
                    }
                    if (product.isFlashSale()) {
                        flashSale.add(product);
                    }
                }

                Log.d(TAG, "Featured products: " + featured.size() + ", Best deals: " + deals.size() + ", Flash sale: " + flashSale.size());

                // Nếu không có sản phẩm featured, hiển thị tất cả sản phẩm
                if (featured.isEmpty() && !products.isEmpty()) {
                    Log.d(TAG, "No featured products found, showing all " + products.size() + " products as featured");
                    featured.addAll(products);
                }

                // Nếu không có best deals, hiển thị tất cả sản phẩm
                if (deals.isEmpty() && !products.isEmpty()) {
                    Log.d(TAG, "No best deals found, showing all " + products.size() + " products as deals");
                    deals.addAll(products);
                }

                // Nếu không có flash sale, hiển thị tất cả sản phẩm
                if (flashSale.isEmpty() && !products.isEmpty()) {
                    Log.d(TAG, "No flash sale products found, showing all " + products.size() + " products as flash sale");
                    flashSale.addAll(products);
                }

                // Log final count
                Log.d(TAG, "Final - Featured: " + featured.size() + ", Deals: " + deals.size());

                // Update cache với data mới
                updateCache(featured, deals, flashSale);

                popularProducts.setValue(featured);
                bestDeals.setValue(deals);
                flashSaleProducts.setValue(flashSale);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load products from Firebase", e);
                isLoading.setValue(false);

                // Hiển thị thông báo lỗi thực tế cho user
                String userFriendlyMessage = e.getMessage();
                if (userFriendlyMessage == null || userFriendlyMessage.isEmpty()) {
                    userFriendlyMessage = "Không thể tải dữ liệu sản phẩm. Vui lòng kiểm tra kết nối mạng và thử lại.";
                }
                errorMessage.setValue(userFriendlyMessage);

                // Không có fallback - chỉ hiển thị lỗi
                popularProducts.setValue(new ArrayList<>());
                bestDeals.setValue(new ArrayList<>());
                Log.d(TAG, "No local fallback - showing error to user");
            }
        });
    }

    public void refreshProducts() {
        Log.d(TAG, "Refreshing products from Firebase...");
        // Clear cache để force reload
        clearCache();
        loadProductsFromFirebase();
    }

    // Force refresh từ Firebase (bỏ qua cache)
    public void forceRefreshFromFirebase() {
        Log.d(TAG, "Force refreshing products from Firebase...");
        isLoading.setValue(true);
        errorMessage.setValue(null);

        productManager.forceRefreshFromFirebase(new ProductManager.OnProductsLoadedListener() {
            @Override
            public void onSuccess(List<Product> products) {
                Log.d(TAG, "Force refresh successful - " + products.size() + " products");
                isLoading.setValue(false);

                List<Product> featured = new ArrayList<>();
                List<Product> deals = new ArrayList<>();
                List<Product> flashSale = new ArrayList<>();

                for (Product product : products) {
                    if (product.isFeatured()) {
                        featured.add(product);
                    }
                    if (product.isBestDeal()) {
                        deals.add(product);
                    }
                    if (product.isFlashSale()) {
                        flashSale.add(product);
                    }
                }

                // Nếu không có sản phẩm featured, hiển thị tất cả sản phẩm
                if (featured.isEmpty() && !products.isEmpty()) {
                    Log.d(TAG, "Force refresh: No featured products, showing all " + products.size() + " products");
                    featured.addAll(products);
                }

                // Nếu không có best deals, hiển thị tất cả sản phẩm
                if (deals.isEmpty() && !products.isEmpty()) {
                    Log.d(TAG, "Force refresh: No deals, showing all " + products.size() + " products");
                    deals.addAll(products);
                }

                // Nếu không có flash sale, hiển thị tất cả sản phẩm
                if (flashSale.isEmpty() && !products.isEmpty()) {
                    Log.d(TAG, "No flash sale products found, showing all " + products.size() + " products as flash sale");
                    flashSale.addAll(products);
                }

                Log.d(TAG, "Force refresh final - Featured: " + featured.size() + ", Deals: " + deals.size() + ", FlashSale: " + flashSale.size());

                updateCache(featured, deals, flashSale);
                popularProducts.setValue(featured);
                bestDeals.setValue(deals);
                flashSaleProducts.setValue(flashSale);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Force refresh failed", e);
                isLoading.setValue(false);

                String userFriendlyMessage = e.getMessage();
                if (userFriendlyMessage == null || userFriendlyMessage.isEmpty()) {
                    userFriendlyMessage = "Không thể làm mới dữ liệu. Vui lòng thử lại sau.";
                }
                errorMessage.setValue(userFriendlyMessage);
            }
        });
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<List<Product>> getPopularProducts() {
        return popularProducts;
    }

    public LiveData<List<Product>> getBestDeals() {
        return bestDeals;
    }

    public LiveData<List<Product>> getFlashSaleProducts() {
        return flashSaleProducts;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
