package com.example.phoneshopapp.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.phoneshopapp.CartManager;
import com.example.phoneshopapp.CategoryAdapter;
import com.example.phoneshopapp.FlashSaleAdapter;
import com.example.phoneshopapp.ProductAdapter;
import com.example.phoneshopapp.ProductGridAdapter;
import com.example.phoneshopapp.ProductManager;
import com.example.phoneshopapp.R;
import com.example.phoneshopapp.adapters.BannerAdapter;
import com.example.phoneshopapp.SearchActivity;
import android.content.Intent;

import android.widget.ImageView;
import android.graphics.PorterDuff;

import com.example.phoneshopapp.databinding.FragmentHomeBinding;
import com.example.phoneshopapp.models.Banner;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private CategoryAdapter categoryAdapter;
    private ProductGridAdapter popularProductsAdapter;
    private ProductAdapter bestDealsAdapter;
    private FlashSaleAdapter flashSaleAdapter;
    private BannerAdapter bannerAdapter;
    private ViewPager2 bannerViewPager;
    private LinearLayout dotsIndicator;
    private Handler bannerHandler;
    private Runnable bannerRunnable;
    private int currentBannerPosition = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "🏠 HomeFragment onCreateView");

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupUI();
        observeData();

        Log.d(TAG, "✅ HomeFragment setup complete");
        return root;
    }

    private void setupUI() {
        View root = binding.getRoot();
        setupBannerSlider(root);
        
        binding.homeSearchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("search_query", query);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        ImageView searchIcon = binding.homeSearchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        searchIcon.setColorFilter(getResources().getColor(R.color.text_secondary), android.graphics.PorterDuff.Mode.SRC_IN);

        // Categories RecyclerView
        categoryAdapter = new CategoryAdapter(new ArrayList<>());
        binding.recyclerCategories.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerCategories.setAdapter(categoryAdapter);

        // Popular Products RecyclerView
        popularProductsAdapter = new ProductGridAdapter(new ArrayList<>());
        binding.recyclerPopularProducts.setLayoutManager(
                new GridLayoutManager(getContext(), 2));
        binding.recyclerPopularProducts.setAdapter(popularProductsAdapter);

        // Best Deals RecyclerView
        bestDealsAdapter = new ProductAdapter(new ArrayList<>());
        binding.recyclerBestDeals.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerBestDeals.setAdapter(bestDealsAdapter);

        // Flash Sale RecyclerView
        flashSaleAdapter = new FlashSaleAdapter(new ArrayList<>());
        binding.recyclerFlashSale.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerFlashSale.setAdapter(flashSaleAdapter);
    }

    private void observeData() {
        // Categories
        homeViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            Log.d(TAG, "📂 Categories updated: " + categories.size());
            categoryAdapter.categoryList = categories;
            categoryAdapter.notifyDataSetChanged();
        });

        // Popular Products
        homeViewModel.getPopularProducts().observe(getViewLifecycleOwner(), products -> {
            Log.d(TAG, "⭐ Popular products updated: " + products.size());
            if (products != null && !products.isEmpty()) {
                popularProductsAdapter.productList = products;
                popularProductsAdapter.notifyDataSetChanged();
                binding.recyclerPopularProducts.setVisibility(View.VISIBLE);

                // Log products for debugging
                for (int i = 0; i < Math.min(5, products.size()); i++) {
                    Log.d(TAG, "Popular: " + products.get(i).getName());
                }
            } else {
                // Không có dữ liệu - ẩn RecyclerView
                binding.recyclerPopularProducts.setVisibility(View.GONE);
                Log.d(TAG, "No popular products to display");
            }
        });

        // Best Deals
        homeViewModel.getBestDeals().observe(getViewLifecycleOwner(), products -> {
            Log.d(TAG, "🔥 Best deals updated: " + products.size());
            if (products != null && !products.isEmpty()) {
                bestDealsAdapter.productList = products;
                bestDealsAdapter.notifyDataSetChanged();
                binding.recyclerBestDeals.setVisibility(View.VISIBLE);

                // Log products for debugging
                for (int i = 0; i < Math.min(5, products.size()); i++) {
                    Log.d(TAG, "Deal: " + products.get(i).getName());
                }
            } else {
                // Không có dữ liệu - ẩn RecyclerView
                binding.recyclerBestDeals.setVisibility(View.GONE);
                Log.d(TAG, "No best deals to display");
            }
        });

        // Flash Sale Products
        homeViewModel.getFlashSaleProducts().observe(getViewLifecycleOwner(), products -> {
            Log.d(TAG, "⚡ Flash sale products updated: " + products.size());
            if (products != null && !products.isEmpty()) {
                flashSaleAdapter.productList = products;
                flashSaleAdapter.notifyDataSetChanged();
                binding.recyclerFlashSale.setVisibility(View.VISIBLE);

                // Log products for debugging
                for (int i = 0; i < Math.min(5, products.size()); i++) {
                    Log.d(TAG, "Flash Sale: " + products.get(i).getName());
                }
            } else {
                // Không có dữ liệu - ẩn RecyclerView
                binding.recyclerFlashSale.setVisibility(View.GONE);
                Log.d(TAG, "No flash sale products to display");
            }
        });

        // Loading State
        homeViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG, "⏳ Loading: " + isLoading);

            // Hiển thị/ẩn loading indicator
            if (isLoading) {
                // Có thể add ProgressBar hoặc loading animation
                Log.d(TAG, "Showing loading state");
            } else {
                Log.d(TAG, "Hiding loading state");
            }
        });

        // Error Messages
        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "❌ Firebase Error: " + error);

                // Hiển thị error với Toast và action để retry
                Toast.makeText(getContext(),
                        error + "\n\nBấm để thử lại",
                        Toast.LENGTH_LONG).show();

                // Có thể thêm error view hoặc retry button ở đây
                showErrorState(error);
            } else {
                hideErrorState();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoScroll();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "📱 Fragment resumed - force refreshing data");
        // Force refresh data mỗi khi user quay lại trang home
        homeViewModel.forceRefreshFromFirebase();
        
        // Restart auto scroll if banner exists
        if (bannerAdapter != null && bannerAdapter.getItemCount() > 1) {
            setupAutoScroll(bannerAdapter.getItemCount());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "🗑️ Fragment destroyed");
        stopAutoScroll();
        binding = null;
    }

    // Method để refresh data
    public void refreshData() {
        Log.d(TAG, "🔄 Manual refresh requested");
        homeViewModel.refreshProducts();
    }

    // Method để hiển thị error state
    private void showErrorState(String errorMessage) {
        Log.d(TAG, "Showing error state: " + errorMessage);
        // Ẩn tất cả RecyclerViews khi có lỗi
        binding.recyclerPopularProducts.setVisibility(View.GONE);
        binding.recyclerBestDeals.setVisibility(View.GONE);

        // Có thể thêm error view hoặc empty state view ở đây
    }

    // Method để ẩn error state
    private void hideErrorState() {
        Log.d(TAG, "Hiding error state");
        // Hiển thị lại RecyclerViews nếu có dữ liệu
        // (sẽ được handle trong observer của products)
    }

    // Method để force refresh từ Firebase
    public void forceRefreshFromFirebase() {
        Log.d(TAG, "🔄 Force refresh from Firebase requested");
        homeViewModel.forceRefreshFromFirebase();
    }
    
    private void setupBannerSlider(View root) {
        bannerViewPager = root.findViewById(R.id.viewPagerBanners);
        dotsIndicator = root.findViewById(R.id.dotsIndicator);
        
        // Tạo sample banner data
        List<Banner> banners = createSampleBanners();
        
        // Setup adapter
        bannerAdapter = new BannerAdapter(banners);
        bannerAdapter.setOnBannerClickListener(banner -> {
            Toast.makeText(getContext(), 
                "Banner clicked: " + banner.getTitle(), 
                Toast.LENGTH_SHORT).show();
        });
        
        bannerViewPager.setAdapter(bannerAdapter);
        
        // Setup dots indicator
        setupDotsIndicator(banners.size());
        
        // Setup auto scroll
        setupAutoScroll(banners.size());
        
        // Listen for page changes
        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentBannerPosition = position;
                updateDotsIndicator(position);
            }
        });
    }
    
    private List<Banner> createSampleBanners() {
        List<Banner> banners = new ArrayList<>();
        
        banners.add(new Banner(
            "iPhone 16 Pro Max",
            "Giảm giá lên đến 20%",
            "Mua Ngay",
            R.drawable.banner_iphone16 // You can change this later
        ));
        
        banners.add(new Banner(
            "Samsung Galaxy S24",
            "Ưu đãi đặc biệt",
            "Khám Phá",
            R.drawable.banner_galaxy
        ));
        
        banners.add(new Banner(
            "Phụ Kiện Hot",
            "Miễn phí vận chuyển",
            "Xem Thêm",
            R.drawable.banner_phukien
        ));
        
        return banners;
    }
    
    private void setupDotsIndicator(int count) {
        dotsIndicator.removeAllViews();
        
        for (int i = 0; i < count; i++) {
            View dot = new View(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.dot_size),
                getResources().getDimensionPixelSize(R.dimen.dot_size)
            );
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            
            if (i == 0) {
                dot.setBackgroundResource(R.drawable.dot_active);
            } else {
                dot.setBackgroundResource(R.drawable.dot_inactive);
            }
            
            dotsIndicator.addView(dot);
        }
    }
    
    private void updateDotsIndicator(int position) {
        for (int i = 0; i < dotsIndicator.getChildCount(); i++) {
            View dot = dotsIndicator.getChildAt(i);
            if (i == position) {
                dot.setBackgroundResource(R.drawable.dot_active);
            } else {
                dot.setBackgroundResource(R.drawable.dot_inactive);
            }
        }
    }
    
    private void setupAutoScroll(int bannerCount) {
        bannerHandler = new Handler(Looper.getMainLooper());
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                if (bannerCount > 1) {
                    currentBannerPosition = (currentBannerPosition + 1) % bannerCount;
                    bannerViewPager.setCurrentItem(currentBannerPosition, true);
                    bannerHandler.postDelayed(this, 3000); // Auto scroll every 3 seconds
                }
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 3000);
    }
    
    private void stopAutoScroll() {
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }
}