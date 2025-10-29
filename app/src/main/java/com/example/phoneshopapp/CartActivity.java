package com.example.phoneshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phoneshopapp.adapters.CartAdapter;
import com.example.phoneshopapp.data.cart.CartManager;
import com.example.phoneshopapp.models.CartItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import android.util.Log;
import java.util.List;

public class CartActivity extends AppCompatActivity implements
    CartAdapter.OnCartItemActionListener,
    CartManager.CartUpdateListener {

  private MaterialToolbar toolbar;
  private RecyclerView recyclerViewCart;
  private LinearLayout emptyCartLayout;
  private TextView textTotalPrice, textItemCount;
  private MaterialButton btnContinueShopping, btnClearCart, btnCheckout;

  private CartAdapter cartAdapter;
  private CartManager cartManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_cart);

    // Setup toolbar with back button
    initViews();
    setupToolbar();

    // Check if user is logged in
    UserManager userManager = UserManager.getInstance(this);
    Log.d("CartActivity", "UserManager isLoggedIn: " + userManager.isLoggedIn());

    if (!userManager.isLoggedIn()) {
      Log.e("CartActivity", "User not logged in, redirecting to LoginActivity");
      Toast.makeText(this, "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_LONG).show();
      Intent loginIntent = new Intent(this, LoginActivity.class);
      startActivity(loginIntent);
      finish();
      return;
    }

    setupRecyclerView();
    setupClickListeners();

    // Initialize cart manager
    cartManager = CartManager.getInstance();
    cartManager.initialize(this);
    cartManager.addCartUpdateListener(this);

    Log.d("CartActivity", "CartManager initialized, starting to load cart...");
    // Load cart data
    cartManager.refreshCart();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (cartManager != null) {
      cartManager.removeCartUpdateListener(this);
    }
  }

  private void initViews() {
    toolbar = findViewById(R.id.toolbar);
    recyclerViewCart = findViewById(R.id.recyclerViewCart);
    emptyCartLayout = findViewById(R.id.emptyCartLayout);
    textTotalPrice = findViewById(R.id.textTotalPrice);
    textItemCount = findViewById(R.id.textItemCount);
    btnContinueShopping = findViewById(R.id.btnContinueShopping);
    btnClearCart = findViewById(R.id.btnClearCart);
    btnCheckout = findViewById(R.id.btnCheckout);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // Enable back button
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    // Handle back button clicks
    toolbar.setNavigationOnClickListener(v -> {
      Log.d("CartActivity", "Back button clicked");
      onBackPressed();
    });
  }

  private void setupRecyclerView() {
    cartAdapter = new CartAdapter(this);
    recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
    recyclerViewCart.setAdapter(cartAdapter);
  }

  private void setupClickListeners() {
    btnContinueShopping.setOnClickListener(v -> {
      // Navigate back to home/product listing
      finish();
    });

    btnClearCart.setOnClickListener(v -> {
      showClearCartConfirmation();
    });

    btnCheckout.setOnClickListener(v -> {
      if (cartManager.isEmpty()) {
        Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
        return;
      }

      // Navigate to checkout activity
      Intent checkoutIntent = new Intent(this, CheckoutActivity.class);
      startActivity(checkoutIntent);
    });
  }

  private void showClearCartConfirmation() {
    new AlertDialog.Builder(this)
        .setTitle("Xóa giỏ hàng")
        .setMessage("Bạn có chắc muốn xóa tất cả sản phẩm trong giỏ hàng?")
        .setPositiveButton("Xóa", (dialog, which) -> {
          cartManager.clearCart(new CartManager.OnCartOperationListener() {
            @Override
            public void onSuccess(String message) {
              runOnUiThread(() -> {
                Toast.makeText(CartActivity.this, message, Toast.LENGTH_SHORT).show();
              });
            }

            @Override
            public void onFailure(String error) {
              runOnUiThread(() -> {
                Toast.makeText(CartActivity.this, error, Toast.LENGTH_LONG).show();
              });
            }
          });
        })
        .setNegativeButton("Hủy", null)
        .show();
  }

  private void updateUI(List<CartItem> cartItems) {
    if (cartItems.isEmpty()) {
      // Show empty cart state
      emptyCartLayout.setVisibility(View.VISIBLE);
      recyclerViewCart.setVisibility(View.GONE);
      btnClearCart.setEnabled(false);
      btnCheckout.setEnabled(false);

      textTotalPrice.setText("₫0");
      textItemCount.setText("0 sản phẩm");
    } else {
      // Show cart items
      emptyCartLayout.setVisibility(View.GONE);
      recyclerViewCart.setVisibility(View.VISIBLE);
      btnClearCart.setEnabled(true);
      btnCheckout.setEnabled(true);

      // Update adapter
      cartAdapter.updateCartItems(cartItems);

      // Update summary
      double totalPrice = cartManager.getTotalPrice();
      int totalCount = cartManager.getTotalItemCount();
      int uniqueCount = cartManager.getUniqueItemCount();

      textTotalPrice.setText(String.format("₫%.0f", totalPrice));
      textItemCount.setText(String.format("%d sản phẩm (%d loại)", totalCount, uniqueCount));
    }
  }

  // CartAdapter.OnCartItemActionListener implementation
  @Override
  public void onQuantityChanged(CartItem item, int newQuantity) {
    cartManager.updateCartItemQuantity(item.getId(), newQuantity, new CartManager.OnCartOperationListener() {
      @Override
      public void onSuccess(String message) {
        // UI will be updated via CartUpdateListener
      }

      @Override
      public void onFailure(String error) {
        runOnUiThread(() -> {
          Toast.makeText(CartActivity.this, error, Toast.LENGTH_SHORT).show();
          // Refresh cart to revert changes
          cartManager.refreshCart();
        });
      }
    });
  }

  @Override
  public void onItemRemoved(CartItem item) {
    cartManager.removeFromCart(item.getId(), new CartManager.OnCartOperationListener() {
      @Override
      public void onSuccess(String message) {
        runOnUiThread(() -> {
          Toast.makeText(CartActivity.this, message, Toast.LENGTH_SHORT).show();
        });
      }

      @Override
      public void onFailure(String error) {
        runOnUiThread(() -> {
          Toast.makeText(CartActivity.this, error, Toast.LENGTH_SHORT).show();
        });
      }
    });
  }

  // CartManager.CartUpdateListener implementation
  @Override
  public void onCartUpdated(List<CartItem> cartItems) {
    Log.d("CartActivity", "onCartUpdated called with " + cartItems.size() + " items");
    runOnUiThread(() -> {
      updateUI(cartItems);
    });
  }

  @Override
  public void onCartCountChanged(int count) {
    Log.d("CartActivity", "onCartCountChanged: " + count);
    // This will be handled in onCartUpdated
  }

  @Override
  public void onCartError(String message) {
    Log.e("CartActivity", "onCartError: " + message);
    runOnUiThread(() -> {
      Toast.makeText(this, "Lỗi giỏ hàng: " + message, Toast.LENGTH_LONG).show();
    });
  }

  @Override
  public boolean onSupportNavigateUp() {
    Log.d("CartActivity", "onSupportNavigateUp called");
    onBackPressed();
    return true;
  }

  @Override
  public void onBackPressed() {
    Log.d("CartActivity", "onBackPressed called");
    super.onBackPressed();
    // Navigate back to MainActivity or previous activity
    finish();
  }
}