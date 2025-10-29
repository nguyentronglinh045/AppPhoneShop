package com.example.phoneshopapp.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.phoneshopapp.databinding.FragmentDashboardBinding;
import com.google.android.material.card.MaterialCardView;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.phoneshopapp.Product;
import com.example.phoneshopapp.ProductAdapter;
import com.example.phoneshopapp.ProductManager;
import com.example.phoneshopapp.R;
import com.example.phoneshopapp.databinding.FragmentDashboardBinding;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private RecyclerView recyclerViewProducts;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText searchEditText;
    private ImageView filterButton;
    private TextView sortButton, resultCountText;
    private LinearLayout loadingView, emptyView;
    private MaterialCardView categoryAll, categoryIphone, categorySamsung, categoryXiaomi, categoryOppo;

    private ProductAdapter productAdapter;
    private List<Product> allProducts = new ArrayList<>();
    private String selectedCategory = "All";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        loadProducts();

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
        categoryAll = binding.categoryAll;
        categoryIphone = binding.categoryIphone;
        categorySamsung = binding.categorySamsung;
        categoryXiaomi = binding.categoryXiaomi;
        categoryOppo = binding.categoryOppo;
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerViewProducts.setLayoutManager(layoutManager);
        productAdapter = new ProductAdapter(new ArrayList<>());
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void setupClickListeners() {
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            String searchQuery = searchEditText.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                performSearch(searchQuery);
            }
            return true;
        });
        filterButton.setOnClickListener(v -> showFilterDialog());
        sortButton.setOnClickListener(v -> showSortDialog());
        categoryAll.setOnClickListener(v -> selectCategory("All"));
        categoryIphone.setOnClickListener(v -> selectCategory("iPhone"));
        categorySamsung.setOnClickListener(v -> selectCategory("Samsung"));
        categoryXiaomi.setOnClickListener(v -> selectCategory("Xiaomi"));
        categoryOppo.setOnClickListener(v -> selectCategory("Oppo"));
        swipeRefreshLayout.setOnRefreshListener(this::loadProducts);
    }

    private void loadProducts() {
        showLoading(true);
        ProductManager.getInstance().loadProductsFromFirebase(new ProductManager.OnProductsLoadedListener() {
            @Override
            public void onSuccess(List<Product> products) {
                allProducts = products;
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                filterAndShowProducts();
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                showEmptyView(true, e.getMessage());
            }
        });
    }

    private void filterAndShowProducts() {
        List<Product> filtered = new ArrayList<>();
        if (selectedCategory.equals("All")) {
            filtered.addAll(allProducts);
        } else {
            for (Product p : allProducts) {
                if (p.getCategory() != null && p.getCategory().equalsIgnoreCase(selectedCategory)) {
                    filtered.add(p);
                }
            }
        }
        productAdapter.updateData(filtered);
        resultCountText.setText("Hiển thị " + filtered.size() + " sản phẩm");
        showEmptyView(filtered.isEmpty(), "Không có sản phẩm nào trong mục này");
    }

    private void performSearch(String query) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            if (p.getName() != null && p.getName().toLowerCase().contains(query.toLowerCase())) {
                if (selectedCategory.equals("All") || (p.getCategory() != null && p.getCategory().equalsIgnoreCase(selectedCategory))) {
                    filtered.add(p);
                }
            }
        }
        productAdapter.updateData(filtered);
        resultCountText.setText("Tìm thấy " + filtered.size() + " sản phẩm");
        showEmptyView(filtered.isEmpty(), "Không tìm thấy sản phẩm phù hợp");
    }

    private void showFilterDialog() {
        Toast.makeText(getContext(), "Filter dialog", Toast.LENGTH_SHORT).show();
    }

    private void showSortDialog() {
        Toast.makeText(getContext(), "Sort dialog", Toast.LENGTH_SHORT).show();
    }

    private void selectCategory(String category) {
        selectedCategory = category;
        filterAndShowProducts();
        updateCategorySelection(category);
    }

    private void updateCategorySelection(String selectedCategory) {
        // Đặt lại trạng thái cho tất cả category
        categoryAll.setStrokeColor(requireContext().getColor(selectedCategory.equals("All") ? R.color.primary : R.color.stroke_light));
        categoryIphone.setStrokeColor(requireContext().getColor(selectedCategory.equals("iPhone") ? R.color.primary : R.color.stroke_light));
        categorySamsung.setStrokeColor(requireContext().getColor(selectedCategory.equals("Samsung") ? R.color.primary : R.color.stroke_light));
        categoryXiaomi.setStrokeColor(requireContext().getColor(selectedCategory.equals("Xiaomi") ? R.color.primary : R.color.stroke_light));
        categoryOppo.setStrokeColor(requireContext().getColor(selectedCategory.equals("Oppo") ? R.color.primary : R.color.stroke_light));
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
        binding = null;
    }
}