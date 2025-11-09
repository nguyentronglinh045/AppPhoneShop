package com.example.phoneshopapp.ui.dashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.phoneshopapp.ProductAdapter;
import com.example.phoneshopapp.R;
import com.example.phoneshopapp.databinding.FragmentDashboardBinding;
import com.google.android.material.card.MaterialCardView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DashboardFragment extends Fragment {
    
    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    
    private RecyclerView recyclerViewProducts;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText searchEditText;
    private ImageView filterButton;
    private TextView sortButton, resultCountText;
    private LinearLayout loadingView, emptyView;
    private HorizontalScrollView categoryScrollView;
    private LinearLayout categoryContainer;

    private ProductAdapter productAdapter;
    private Handler searchHandler;
    private Runnable searchRunnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        searchHandler = new Handler(Looper.getMainLooper());

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();

        return root;
    }

    private void initializeViews() {
        recyclerViewProducts = binding.recyclerViewProducts;
        swipeRefreshLayout = binding.swipeRefreshLayout;
        searchEditText = binding.searchEditText;
        filterButton = binding.filterButton;
        sortButton = binding.sortButton;
        resultCountText = binding.resultCountText;
        loadingView = binding.loadingView;
        emptyView = binding.emptyView;
        
        // Get category container - tìm HorizontalScrollView trong layout
        View parent = binding.getRoot();
        categoryScrollView = findCategoryScrollView(parent);
        if (categoryScrollView != null && categoryScrollView.getChildCount() > 0) {
            View child = categoryScrollView.getChildAt(0);
            if (child instanceof LinearLayout) {
                categoryContainer = (LinearLayout) child;
            }
        }
    }
    
    private HorizontalScrollView findCategoryScrollView(View parent) {
        if (parent instanceof HorizontalScrollView) {
            return (HorizontalScrollView) parent;
        }
        if (parent instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) parent;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                HorizontalScrollView result = findCategoryScrollView(child);
                if (result != null) return result;
            }
        }
        return null;
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerViewProducts.setLayoutManager(layoutManager);
        productAdapter = new ProductAdapter(java.util.Collections.emptyList());
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void setupClickListeners() {
        // Real-time search với debounce
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> viewModel.searchProducts(s.toString().trim());
                searchHandler.postDelayed(searchRunnable, 300);
            }
        });
        
        filterButton.setOnClickListener(v -> showFilterDialog());
        sortButton.setOnClickListener(v -> showSortDialog());
        swipeRefreshLayout.setOnRefreshListener(() -> viewModel.loadProducts());
        
        // Clear filters button
        View clearFiltersButton = emptyView.findViewById(R.id.clearFiltersButton);
        if (clearFiltersButton != null) {
            clearFiltersButton.setOnClickListener(v -> {
                searchEditText.setText("");
                viewModel.clearFilters();
            });
        }
    }

    private void observeViewModel() {
        // Observe filtered products
        viewModel.getFilteredProducts().observe(getViewLifecycleOwner(), products -> {
            productAdapter.updateData(products);
            if (products.isEmpty()) {
                showEmptyView(true, "Không có sản phẩm nào");
            } else {
                showEmptyView(false, "");
            }
        });
        
        // Observe categories - dynamic categories
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            updateCategoryChips(categories);
        });
        
        // Observe selected category
        viewModel.getSelectedCategory().observe(getViewLifecycleOwner(), category -> {
            updateCategorySelection(category);
        });
        
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            showLoading(isLoading);
            swipeRefreshLayout.setRefreshing(isLoading);
        });
        
        // Observe result count
        viewModel.getResultCount().observe(getViewLifecycleOwner(), count -> {
            resultCountText.setText("Hiển thị " + count + " sản phẩm");
        });
        
        // Observe error
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateCategoryChips(List<String> categories) {
        if (categoryContainer == null) return;
        
        categoryContainer.removeAllViews();
        
        for (String category : categories) {
            MaterialCardView chip = createCategoryChip(category);
            categoryContainer.addView(chip);
        }
    }
    
    private MaterialCardView createCategoryChip(String category) {
        MaterialCardView chip = new MaterialCardView(requireContext());
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMarginEnd((int) (8 * getResources().getDisplayMetrics().density));
        chip.setLayoutParams(params);
        
        chip.setRadius(20 * getResources().getDisplayMetrics().density);
        chip.setCardElevation(2 * getResources().getDisplayMetrics().density);
        chip.setCardBackgroundColor(requireContext().getColor(
            category.equals("All") ? R.color.primary : R.color.white));
        chip.setStrokeWidth((int) (1 * getResources().getDisplayMetrics().density));
        chip.setStrokeColor(requireContext().getColor(R.color.stroke_light));
        chip.setClickable(true);
        chip.setFocusable(true);
        chip.setRippleColor(android.content.res.ColorStateList.valueOf(
            requireContext().getColor(R.color.primary_light)));
        
        TextView textView = new TextView(requireContext());
        textView.setText(category);
        textView.setTextColor(requireContext().getColor(
            category.equals("All") ? R.color.white : R.color.text_primary));
        textView.setTextSize(14);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        textView.setPadding(padding, padding / 2, padding, padding / 2);
        
        chip.addView(textView);
        chip.setOnClickListener(v -> viewModel.setSelectedCategory(category));
        
        return chip;
    }
    
    private void updateCategorySelection(String selectedCategory) {
        if (categoryContainer == null) return;
        
        for (int i = 0; i < categoryContainer.getChildCount(); i++) {
            View child = categoryContainer.getChildAt(i);
            if (child instanceof MaterialCardView) {
                MaterialCardView chip = (MaterialCardView) child;
                if (chip.getChildCount() > 0 && chip.getChildAt(0) instanceof TextView) {
                    TextView textView = (TextView) chip.getChildAt(0);
                    String category = textView.getText().toString();
                    
                    boolean isSelected = category.equals(selectedCategory);
                    chip.setCardBackgroundColor(requireContext().getColor(
                        isSelected ? R.color.primary : R.color.white));
                    chip.setStrokeColor(requireContext().getColor(
                        isSelected ? R.color.primary : R.color.stroke_light));
                    textView.setTextColor(requireContext().getColor(
                        isSelected ? R.color.white : R.color.text_primary));
                }
            }
        }
    }

    private void showSortDialog() {
        String[] sortOptions = {
            "Mặc định",
            "Giá: Thấp đến Cao",
            "Giá: Cao đến Thấp",
            "Tên: A-Z",
            "Tên: Z-A"
        };
        
        String[] sortValues = {
            "default",
            "price_asc",
            "price_desc",
            "name_asc",
            "name_desc"
        };
        
        new AlertDialog.Builder(requireContext())
            .setTitle("Sắp xếp theo")
            .setItems(sortOptions, (dialog, which) -> {
                viewModel.setSelectedSort(sortValues[which]);
                sortButton.setText(sortOptions[which]);
            })
            .show();
    }

    private void showFilterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        
        // Get max price từ ViewModel
        double maxPrice = viewModel.getMaxProductPrice();
        
        // TODO: Setup price range slider và brand checkboxes
        // Đơn giản hóa cho dự án học tập - chỉ filter theo brand
        
        Set<String> allBrands = viewModel.getAllBrands();
        LinearLayout brandContainer = dialogView.findViewById(R.id.brandContainer);
        
        if (brandContainer != null && !allBrands.isEmpty()) {
            Set<String> selectedBrands = new HashSet<>();
            
            for (String brand : allBrands) {
                android.widget.CheckBox checkBox = new android.widget.CheckBox(requireContext());
                checkBox.setText(brand);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedBrands.add(brand);
                    } else {
                        selectedBrands.remove(brand);
                    }
                });
                brandContainer.addView(checkBox);
            }
            
            new AlertDialog.Builder(requireContext())
                .setTitle("Lọc sản phẩm")
                .setView(dialogView)
                .setPositiveButton("Áp dụng", (dialog, which) -> {
                    viewModel.setSelectedBrands(selectedBrands);
                })
                .setNegativeButton("Hủy", null)
                .setNeutralButton("Xóa bộ lọc", (dialog, which) -> {
                    viewModel.clearFilters();
                })
                .show();
        } else {
            Toast.makeText(getContext(), "Chưa có sản phẩm để lọc", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewProducts.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    private void showEmptyView(boolean show, String message) {
        emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewProducts.setVisibility(show ? View.GONE : View.VISIBLE);
        if (show) {
            TextView emptyText = emptyView.findViewById(R.id.textEmptyTitle);
            if (emptyText != null) emptyText.setText(message);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        binding = null;
    }
}
