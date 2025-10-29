package com.example.phoneshopapp;

import android.util.Log;
import com.example.phoneshopapp.data.ProductData;
import java.util.List;

public class ProductManager {
  private static final String TAG = "ProductManager";
  private static ProductManager instance;

  // Singleton pattern để đảm bảo chỉ có 1 instance
  public static ProductManager getInstance() {
    if (instance == null) {
      instance = new ProductManager();
    }
    return instance;
  }

  private ProductManager() {
    // Private constructor
  }

  // Interface cho callback khi load Firebase data
  public interface OnProductsLoadedListener {
    void onSuccess(List<Product> products);

    void onFailure(Exception e);
  }

  // Load products từ Firebase (method chính)
  public void loadProductsFromFirebase(OnProductsLoadedListener listener) {
    Log.d(TAG, "Loading products from Firebase via ProductManager...");

    ProductData.loadProductsFromFirebase(new ProductData.OnProductsLoadedListener() {
      @Override
      public void onSuccess(List<Product> products) {
        Log.d(TAG, "ProductManager received " + products.size() + " products from Firebase");
        listener.onSuccess(products);
      }

      @Override
      public void onFailure(Exception e) {
        Log.e(TAG, "ProductManager failed to load from Firebase", e);
        listener.onFailure(e);
      }
    });
  }

  // Force refresh từ Firebase
  public void forceRefreshFromFirebase(OnProductsLoadedListener listener) {
    Log.d(TAG, "Force refreshing products from Firebase...");
    ProductData.forceRefreshFromFirebase(new ProductData.OnProductsLoadedListener() {
      @Override
      public void onSuccess(List<Product> products) {
        Log.d(TAG, "ProductManager force refresh successful - " + products.size() + " products");
        listener.onSuccess(products);
      }

      @Override
      public void onFailure(Exception e) {
        Log.e(TAG, "ProductManager force refresh failed", e);
        listener.onFailure(e);
      }
    });
  }

  // Helper interface for single product lookup
  public interface OnSingleProductLoadedListener {
    void onSuccess(Product product);

    void onFailure(Exception e);
  }

  // Find product by ID
  public void findProductById(String productId, OnSingleProductLoadedListener listener) {
    Log.d(TAG, "Finding product by ID: " + productId);

    loadProductsFromFirebase(new OnProductsLoadedListener() {
      @Override
      public void onSuccess(List<Product> products) {
        for (Product product : products) {
          if (product != null && product.getId() != null && product.getId().equals(productId)) {
            Log.d(TAG, "Product found: " + product.getName());
            listener.onSuccess(product);
            return;
          }
        }
        Log.w(TAG, "Product not found with ID: " + productId);
        listener.onFailure(new Exception("Product not found with ID: " + productId));
      }

      @Override
      public void onFailure(Exception e) {
        Log.e(TAG, "Failed to find product by ID", e);
        listener.onFailure(e);
      }
    });
  }
}