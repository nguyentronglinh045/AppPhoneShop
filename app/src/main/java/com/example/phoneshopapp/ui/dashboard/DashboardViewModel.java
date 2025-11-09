package com.example.phoneshopapp.ui.dashboard;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phoneshopapp.Product;
import com.example.phoneshopapp.ProductManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DashboardViewModel extends ViewModel {
    
    private static final String TAG = "DashboardViewModel";
    
    private final MutableLiveData<List<Product>> allProducts;
    private final MutableLiveData<List<Product>> filteredProducts;
    private final MutableLiveData<List<String>> categories;
    private final MutableLiveData<String> selectedCategory;
    private final MutableLiveData<String> selectedSort;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    private final MutableLiveData<Integer> resultCount;
    
    private final ProductManager productManager;
    private double minPrice = 0;
    private double maxPrice = Double.MAX_VALUE;
    private Set<String> selectedBrands = new HashSet<>();
    
    public DashboardViewModel() {
        allProducts = new MutableLiveData<>(new ArrayList<>());
        filteredProducts = new MutableLiveData<>(new ArrayList<>());
        categories = new MutableLiveData<>(new ArrayList<>());
        selectedCategory = new MutableLiveData<>("All");
        selectedSort = new MutableLiveData<>("default");
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>("");
        resultCount = new MutableLiveData<>(0);
        
        productManager = ProductManager.getInstance();
        
        loadProducts();
    }
    
    public void loadProducts() {
        Log.d(TAG, "Loading products from Firebase");
        isLoading.setValue(true);
        errorMessage.setValue("");
        
        productManager.loadProductsFromFirebase(new ProductManager.OnProductsLoadedListener() {
            @Override
            public void onSuccess(List<Product> products) {
                Log.d(TAG, "Products loaded successfully: " + products.size());
                allProducts.setValue(products);
                isLoading.setValue(false);
                
                extractCategories(products);
                applyFiltersAndSort();
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load products: " + e.getMessage());
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi tải sản phẩm: " + e.getMessage());
            }
        });
    }
    
    private void extractCategories(List<Product> products) {
        Set<String> categorySet = new HashSet<>();
        categorySet.add("All");
        
        for (Product product : products) {
            if (product.getCategory() != null && !product.getCategory().isEmpty()) {
                categorySet.add(product.getCategory());
            }
        }
        
        List<String> categoryList = new ArrayList<>(categorySet);
        Collections.sort(categoryList, (a, b) -> {
            if (a.equals("All")) return -1;
            if (b.equals("All")) return 1;
            return a.compareTo(b);
        });
        
        categories.setValue(categoryList);
        Log.d(TAG, "Categories extracted: " + categoryList.size());
    }
    
    public void setSelectedCategory(String category) {
        selectedCategory.setValue(category);
        applyFiltersAndSort();
    }
    
    public void setSelectedSort(String sort) {
        selectedSort.setValue(sort);
        applyFiltersAndSort();
    }
    
    public void setPriceRange(double min, double max) {
        this.minPrice = min;
        this.maxPrice = max;
        applyFiltersAndSort();
    }
    
    public void setSelectedBrands(Set<String> brands) {
        this.selectedBrands = new HashSet<>(brands);
        applyFiltersAndSort();
    }
    
    public void clearFilters() {
        selectedCategory.setValue("All");
        selectedSort.setValue("default");
        minPrice = 0;
        maxPrice = Double.MAX_VALUE;
        selectedBrands.clear();
        applyFiltersAndSort();
    }
    
    public void searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            applyFiltersAndSort();
            return;
        }
        
        String lowerQuery = query.toLowerCase().trim();
        List<Product> products = allProducts.getValue();
        if (products == null) return;
        
        List<Product> searchResults = new ArrayList<>();
        
        for (Product product : products) {
            boolean matches = false;
            
            // Search in name
            if (product.getName() != null && 
                product.getName().toLowerCase().contains(lowerQuery)) {
                matches = true;
            }
            
            // Search in brand
            if (product.getBrand() != null && 
                product.getBrand().toLowerCase().contains(lowerQuery)) {
                matches = true;
            }
            
            // Search in category
            if (product.getCategory() != null && 
                product.getCategory().toLowerCase().contains(lowerQuery)) {
                matches = true;
            }
            
            if (matches) {
                searchResults.add(product);
            }
        }
        
        // Apply category filter
        String category = selectedCategory.getValue();
        if (category != null && !category.equals("All")) {
            List<Product> categoryFiltered = new ArrayList<>();
            for (Product p : searchResults) {
                if (p.getCategory() != null && p.getCategory().equals(category)) {
                    categoryFiltered.add(p);
                }
            }
            searchResults = categoryFiltered;
        }
        
        applySortToList(searchResults);
        filteredProducts.setValue(searchResults);
        resultCount.setValue(searchResults.size());
        
        Log.d(TAG, "Search: " + searchResults.size() + " results for: " + query);
    }
    
    private void applyFiltersAndSort() {
        List<Product> products = allProducts.getValue();
        if (products == null || products.isEmpty()) {
            filteredProducts.setValue(new ArrayList<>());
            resultCount.setValue(0);
            return;
        }
        
        List<Product> filtered = new ArrayList<>();
        String category = selectedCategory.getValue();
        
        for (Product product : products) {
            // Category filter
            if (category != null && !category.equals("All")) {
                if (product.getCategory() == null || !product.getCategory().equals(category)) {
                    continue;
                }
            }
            
            // Price filter - FIXED: use getPriceValue() instead of getPrice()
            if (product.getPriceValue() < minPrice || product.getPriceValue() > maxPrice) {
                continue;
            }
            
            // Brand filter
            if (!selectedBrands.isEmpty()) {
                if (product.getBrand() == null || !selectedBrands.contains(product.getBrand())) {
                    continue;
                }
            }
            
            filtered.add(product);
        }
        
        applySortToList(filtered);
        filteredProducts.setValue(filtered);
        resultCount.setValue(filtered.size());
        
        Log.d(TAG, "Filters applied: " + filtered.size() + " products");
    }
    
    private void applySortToList(List<Product> products) {
        String sort = selectedSort.getValue();
        if (sort == null || sort.equals("default")) {
            return;
        }
        
        Comparator<Product> comparator = null;
        
        switch (sort) {
            case "price_asc":
                // FIXED: use getPriceValue() instead of getPrice()
                comparator = (a, b) -> Double.compare(a.getPriceValue(), b.getPriceValue());
                break;
            case "price_desc":
                // FIXED: use getPriceValue() instead of getPrice()
                comparator = (a, b) -> Double.compare(b.getPriceValue(), a.getPriceValue());
                break;
            case "name_asc":
                comparator = (a, b) -> {
                    String nameA = a.getName() != null ? a.getName() : "";
                    String nameB = b.getName() != null ? b.getName() : "";
                    return nameA.compareToIgnoreCase(nameB);
                };
                break;
            case "name_desc":
                comparator = (a, b) -> {
                    String nameA = a.getName() != null ? a.getName() : "";
                    String nameB = b.getName() != null ? b.getName() : "";
                    return nameB.compareToIgnoreCase(nameA);
                };
                break;
        }
        
        if (comparator != null) {
            Collections.sort(products, comparator);
        }
    }
    
    // Getters
    public LiveData<List<Product>> getFilteredProducts() {
        return filteredProducts;
    }
    
    public LiveData<List<String>> getCategories() {
        return categories;
    }
    
    public LiveData<String> getSelectedCategory() {
        return selectedCategory;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<Integer> getResultCount() {
        return resultCount;
    }
    
    public Set<String> getAllBrands() {
        List<Product> products = allProducts.getValue();
        Set<String> brands = new HashSet<>();
        
        if (products != null) {
            for (Product p : products) {
                if (p.getBrand() != null && !p.getBrand().isEmpty()) {
                    brands.add(p.getBrand());
                }
            }
        }
        
        return brands;
    }
    
    public double getMaxProductPrice() {
        List<Product> products = allProducts.getValue();
        double max = 0;
        
        if (products != null) {
            for (Product p : products) {
                // FIXED: use getPriceValue() instead of getPrice()
                if (p.getPriceValue() > max) {
                    max = p.getPriceValue();
                }
            }
        }
        
        return max;
    }
}
