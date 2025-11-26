package com.example.phoneshopapp.ui.favorites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phoneshopapp.MainActivity;
import com.example.phoneshopapp.ProductDetailActivity;
import com.example.phoneshopapp.R;
import com.example.phoneshopapp.data.favorite.FavoriteManager;
import com.example.phoneshopapp.models.FavoriteItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity displaying user's favorite products
 * Supports swipe-to-delete and click to view product details
 */
public class FavoritesActivity extends AppCompatActivity 
        implements FavoriteManager.FavoriteUpdateListener {

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private FavoritesAdapter adapter;
    private View layoutEmpty;
    private ProgressBar progressBar;
    private MaterialButton btnExplore;
    private FavoriteManager favoriteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupEmptyState();

        favoriteManager = FavoriteManager.getInstance();
        favoriteManager.initialize(this); // Initialize with context to prevent crash
        favoriteManager.addListener(this);
        
        // Show loading
        showLoading(true);
        
        // Load favorites
        favoriteManager.loadFavorites();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewFavorites);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        btnExplore = findViewById(R.id.btnExplore);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new FavoritesAdapter(this, new ArrayList<>(), 
            new FavoritesAdapter.OnFavoriteItemClickListener() {
                @Override
                public void onItemClick(FavoriteItem item) {
                    // Navigate to product detail
                    Intent intent = new Intent(FavoritesActivity.this, ProductDetailActivity.class);
                    intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, item.getProductId());
                    startActivity(intent);
                }

                @Override
                public void onRemoveClick(FavoriteItem item) {
                    showRemoveConfirmDialog(item);
                }
            });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Setup swipe-to-delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
            new SwipeToDeleteCallback(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void setupEmptyState() {
        btnExplore.setOnClickListener(v -> {
            // Navigate to home screen
            Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void showRemoveConfirmDialog(FavoriteItem item) {
        new AlertDialog.Builder(this)
            .setTitle("Xóa khỏi yêu thích")
            .setMessage("Bạn có chắc muốn xóa \"" + item.getProductName() + "\" khỏi danh sách yêu thích?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                favoriteManager.removeFavorite(item.getId(), new FavoriteManager.OnFavoriteOperationListener() {
                    @Override
                    public void onSuccess(String message) {
                        // Silent success
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(FavoritesActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    // ============================================
    // FavoriteUpdateListener Implementation
    // ============================================

    @Override
    public void onFavoritesUpdated(List<FavoriteItem> favorites) {
        runOnUiThread(() -> {
            showLoading(false);
            adapter.updateData(favorites);
            updateEmptyState(favorites.isEmpty());
            
            // Update toolbar title with count
            if (getSupportActionBar() != null) {
                String title = favorites.isEmpty() ? 
                    "Yêu Thích" : 
                    "Yêu Thích (" + favorites.size() + ")";
                getSupportActionBar().setTitle(title);
            }
        });
    }

    @Override
    public void onFavoriteCountChanged(int count) {
        // Count is already reflected in onFavoritesUpdated
    }

    @Override
    public void onFavoriteError(String message) {
        runOnUiThread(() -> {
            showLoading(false);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        favoriteManager.removeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh favorites when returning to this screen
        favoriteManager.refreshFavorites();
    }
}
