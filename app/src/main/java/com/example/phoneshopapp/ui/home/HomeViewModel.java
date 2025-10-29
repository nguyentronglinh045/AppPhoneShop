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
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    private final ProductManager productManager;

    // Static cache để giữ data giữa các lần navigation
    private static List<Product> cachedPopularProducts = null;
    private static List<Product> cachedBestDeals = null;
    private static long lastCacheTime = 0;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 phút cache

    public HomeViewModel() {
        categories = new MutableLiveData<>();
        popularProducts = new MutableLiveData<>();
        bestDeals = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        productManager = ProductManager.getInstance();

        loadCategories();

        // Luôn load từ Firebase để đảm bảo data mới nhất
        Log.d(TAG, "Force loading fresh data from Firebase on init");
        forceRefreshFromFirebase();
    }

    private void loadCategories() {
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(new Category("Premium Smartphones", R.drawable.ic_home_black_24dp));
        categoryList.add(new Category("Tablets & iPads", R.drawable.ic_dashboard_black_24dp));
        categoryList.add(new Category("Phone Accessories", R.drawable.ic_notifications_black_24dp));
        categoryList.add(new Category("Smart Watches", R.drawable.ic_home_black_24dp));
        categoryList.add(new Category("Audio & Headphones", R.drawable.ic_dashboard_black_24dp));
        categories.setValue(categoryList);
    }

    // Kiểm tra cache có còn valid không
    private boolean isCacheValid() {
        return cachedPopularProducts != null &&
                cachedBestDeals != null &&
                (System.currentTimeMillis() - lastCacheTime) < CACHE_DURATION;
    }

    // Load data từ cache
    private void loadFromCache() {
        isLoading.setValue(false);
        popularProducts.setValue(new ArrayList<>(cachedPopularProducts));
        bestDeals.setValue(new ArrayList<>(cachedBestDeals));
        Log.d(TAG, "Loaded data from cache - Popular: " + cachedPopularProducts.size() +
                ", Deals: " + cachedBestDeals.size());
    }

    // Update cache với data mới
    private void updateCache(List<Product> featured, List<Product> deals) {
        cachedPopularProducts = new ArrayList<>(featured);
        cachedBestDeals = new ArrayList<>(deals);
        lastCacheTime = System.currentTimeMillis();
        Log.d(TAG, "Cache updated with " + featured.size() + " featured and " + deals.size() + " deals");
    }

    // Clear cache (có thể gọi khi cần refresh)
    public static void clearCache() {
        cachedPopularProducts = null;
        cachedBestDeals = null;
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

                for (Product product : products) {
                    Log.d(TAG, "Product: " + product.getName() +
                            " | Featured: " + product.isFeatured() +
                            " | BestDeal: " + product.isBestDeal() +
                            " | ImageUrl: " + product.getImageUrl());

                    if (product.isFeatured()) {
                        featured.add(product);
                    }
                    if (product.isBestDeal()) {
                        deals.add(product);
                    }
                }

                Log.d(TAG, "Featured products: " + featured.size() + ", Best deals: " + deals.size());

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

                // Log final count
                Log.d(TAG, "Final - Featured: " + featured.size() + ", Deals: " + deals.size());

                // Update cache với data mới
                updateCache(featured, deals);

                popularProducts.setValue(featured);
                bestDeals.setValue(deals);
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

                for (Product product : products) {
                    if (product.isFeatured()) {
                        featured.add(product);
                    }
                    if (product.isBestDeal()) {
                        deals.add(product);
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

                Log.d(TAG, "Force refresh final - Featured: " + featured.size() + ", Deals: " + deals.size());

                updateCache(featured, deals);
                popularProducts.setValue(featured);
                bestDeals.setValue(deals);
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

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
