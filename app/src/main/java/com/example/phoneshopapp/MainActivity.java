package com.example.phoneshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.phoneshopapp.data.cart.CartManager;
import com.example.phoneshopapp.databinding.ActivityMainBinding;
import com.example.phoneshopapp.utils.AuthDebugHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements CartManager.CartUpdateListener {
    private UserManager userManager;

    private ActivityMainBinding binding;
    private ProductManager productManager;
    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Khởi tạo UserManager
        userManager = UserManager.getInstance(this);
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo ProductManager
        productManager = ProductManager.getInstance();

        // Test Firebase connection
        testFirebaseConnection();

        // Demo: Thay đổi một số sản phẩm để test
        demoProductChanges();

        // Setup navigation
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        
        // Ẩn mục Admin nếu không phải admin
        if (!"admin".equalsIgnoreCase(userManager.getRole())) {
            binding.navView.getMenu().findItem(R.id.navigation_admin).setVisible(false);
        }

        // Initialize cart manager
        cartManager = CartManager.getInstance();
        cartManager.initialize(this);
        cartManager.addCartUpdateListener(this);

        // Setup cart navigation
        setupCartNavigation(navController);

        // Initialize cart badge
        updateCartBadge();
    }

    private void setupCartNavigation(NavController navController) {
        binding.navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_cart) {
                // Debug auth state before accessing cart
                AuthDebugHelper.debugAuthState(this);

                // Check if user is logged in before navigating to cart
                if (!AuthDebugHelper.isUserFullyAuthenticated(this)) {
                    Toast.makeText(this, "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_LONG).show();
                    // Navigate to login
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    startActivity(loginIntent);
                    return false;
                }

                // Navigate to CartActivity
                Intent intent = new Intent(this, CartActivity.class);
                startActivity(intent);
                // Don't return true vì cart không phải fragment trong bottom nav
                return false;
            } else {
                // Use NavigationUI for fragments - nó tự động sync selection
                return NavigationUI.onNavDestinationSelected(item, navController);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cartManager != null) {
            cartManager.removeCartUpdateListener(this);
        }
    }

    /**
     * DEMO: Test thay đổi sản phẩm từ code
     * Bạn có thể bỏ comment để thấy sự thay đổi
     */
    private void demoProductChanges() {
        // Uncomment những dòng dưới để test thay đổi sản phẩm:

        // 1. Thay đổi tên sản phẩm
        // productManager.updateProductName(1, "iPhone 15 Pro Max (Special Edition)");

        // 2. Thay đổi giá với khuyến mãi
        // productManager.updateProductPrice(4, "$999 (Black Friday!)");

        // 3. Thêm sản phẩm mới
        // Product newPhone = new Product(11, "Google Pixel 8 Pro", "$899",
        // R.drawable.pixel, "Google's flagship with amazing AI camera", "Google",
        // true, false);
        // productManager.addProduct(newPhone);
    }

    /**
     * Test Firebase connection and data loading
     */
    private void testFirebaseConnection() {
        Log.d("Firebase", "Testing Firebase connection...");

        productManager.loadProductsFromFirebase(new ProductManager.OnProductsLoadedListener() {
            @Override
            public void onSuccess(List<Product> products) {
                Log.d("Firebase", "✅ SUCCESS: Loaded " + products.size() + " products from Firebase!");

                // Log chi tiết từng sản phẩm
                for (Product product : products) {
                    Log.d("Firebase", "📱 Product: " + product.getName() +
                            " | Brand: " + product.getBrand() +
                            " | Price: " + product.getPrice() +
                            " | Stock: " + product.getStockQuantity());
                }

                // Bạn có thể update UI ở đây nếu cần
                // updateUIWithFirebaseData(products);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firebase", "❌ FAILED: Could not load products from Firebase", e);
                Log.d("Firebase", "Will use local data as fallback");
            }
        });
    }

    private void updateUIWithFirebaseData(List<Product> products) {
        // TODO: Update UI components với Firebase data nếu cần
        Log.d("Firebase", "UI updated with " + products.size() + " products from Firebase");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Debug authentication state
        AuthDebugHelper.debugAuthState(this);

        if (cartManager != null) {
            cartManager.refreshCart();
        }
        updateCartBadge();
    }

    // CartManager.CartUpdateListener implementations
    @Override
    public void onCartUpdated(List<com.example.phoneshopapp.models.CartItem> cartItems) {
        runOnUiThread(() -> {
            updateCartBadge();
        });
    }

    @Override
    public void onCartCountChanged(int count) {
        runOnUiThread(() -> {
            updateCartBadge();
        });
    }

    @Override
    public void onCartError(String message) {
        runOnUiThread(() -> {
            Log.e("MainActivity", "Cart error: " + message);
            Toast.makeText(this, "Lỗi giỏ hàng: " + message, Toast.LENGTH_LONG).show();
        });
    }

    private void updateCartBadge() {
        if (binding == null || cartManager == null)
            return;

        BottomNavigationView nav = binding.navView;
        int count = cartManager.getTotalItemCount();
        var badge = nav.getOrCreateBadge(R.id.navigation_cart);

        if (count > 0) {
            badge.setVisible(true);
            badge.setNumber(count);
        } else {
            badge.clearNumber();
            badge.setVisible(false);
        }
    }
}
