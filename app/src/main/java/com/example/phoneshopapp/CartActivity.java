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
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements
    CartAdapter.OnCartItemActionListener,
    CartManager.CartUpdateListener {

  private MaterialToolbar toolbar;
  private RecyclerView recyclerViewCart;
  private LinearLayout emptyCartLayout, layoutSelectAll;
  private TextView textTotalPrice, textItemCount;
  private MaterialButton btnContinueShopping, btnClearCart, btnCheckout;
  private com.google.android.material.checkbox.MaterialCheckBox checkboxSelectAll;
  private android.view.View dividerSelectAll;

  private CartAdapter cartAdapter;
  private CartManager cartManager;
  private boolean isFirstLoad = true; // Flag to track first cart load for auto-selection
  private String autoSelectProductId = null; // Product ID to auto-select from "Buy Now"
  private String autoSelectVariantId = null; // Variant ID to auto-select from "Buy Now"

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

    // Get auto-selection info from Intent (from "Buy Now" flow)
    Intent intent = getIntent();
    if (intent != null) {
      autoSelectProductId = intent.getStringExtra("AUTO_SELECT_PRODUCT_ID");
      autoSelectVariantId = intent.getStringExtra("AUTO_SELECT_VARIANT_ID");
      if (autoSelectProductId != null) {
        Log.d("CartActivity", "Auto-select requested for product: " + autoSelectProductId + 
              ", variant: " + (autoSelectVariantId != null ? autoSelectVariantId : "null"));
      }
    }

    setupRecyclerView();
    setupClickListeners();

    // Initialize cart manager
    cartManager = CartManager.getInstance();
    cartManager.initialize(this);
    cartManager.addCartUpdateListener(this);

    Log.d("CartActivity", "CartManager initialized, starting to load cart...");
    // Load cart data (auto-selection will be handled after cart is loaded)
    cartManager.refreshCart();
  }
  
  /**
   * Handle auto-selection of last added item (from "Buy Now" button)
   * This should be called AFTER cart items are loaded AND UI is updated
   */
  private void handleAutoSelection() {
    if (autoSelectProductId == null) {
      Log.d("CartActivity", "No auto-selection needed (autoSelectProductId is null)");
      return; // No auto-selection requested
    }
    
    Log.d("CartActivity", "=== AUTO-SELECTION START ===");
    Log.d("CartActivity", "Searching for item: product=" + autoSelectProductId + 
          ", variant=" + (autoSelectVariantId != null ? autoSelectVariantId : "null"));
    
    // Find the matching cart item by productId and variantId
    List<CartItem> cartItems = cartManager.getCartItems();
    Log.d("CartActivity", "Total cart items to search: " + cartItems.size());
    
    CartItem itemToSelect = null;
    java.util.Date latestDate = null;
    
    for (CartItem item : cartItems) {
      Log.d("CartActivity", "Checking item: id=" + item.getId() + 
            ", productId=" + item.getProductId() + 
            ", variantId='" + item.getVariantId() + "'" +
            ", isSelected=" + item.isSelected());
            
      boolean matchProduct = item.getProductId() != null && item.getProductId().equals(autoSelectProductId);
      
      // Handle both null and empty string cases for variant matching
      String itemVariantId = item.getVariantId();
      String targetVariantId = autoSelectVariantId;
      
      // Normalize empty string to null for comparison
      if (itemVariantId != null && itemVariantId.trim().isEmpty()) {
        itemVariantId = null;
      }
      if (targetVariantId != null && targetVariantId.trim().isEmpty()) {
        targetVariantId = null;
      }
      
      boolean matchVariant;
      if (targetVariantId == null) {
        // If target is null, match items with null or empty variantId
        matchVariant = (itemVariantId == null);
      } else {
        // If target is not null, exact match required
        matchVariant = targetVariantId.equals(itemVariantId);
      }
      
      Log.d("CartActivity", "  matchProduct=" + matchProduct + ", matchVariant=" + matchVariant +
            " (item='" + itemVariantId + "', target='" + targetVariantId + "')");
      
      if (matchProduct && matchVariant) {
        // Find the newest one (in case multiple items match)
        if (itemToSelect == null || (item.getAddedAt() != null &&
            (latestDate == null || item.getAddedAt().after(latestDate)))) {
          itemToSelect = item;
          latestDate = item.getAddedAt();
          Log.d("CartActivity", "  ✓ Selected as candidate (newest so far)");
        }
      }
    }
    
    if (itemToSelect != null) {
      Log.d("CartActivity", "✅ Found item to auto-select: " + itemToSelect.getId());
      
      // Set selection directly on the item object
      itemToSelect.setSelected(true);
      
      // Notify adapter to refresh
      if (cartAdapter != null) {
        cartAdapter.notifyDataSetChanged();
        Log.d("CartActivity", "✅ Adapter notified to refresh");
      }
      
      // Also update in CartManager for consistency
      cartManager.updateItemSelection(itemToSelect.getId(), true);
      
      Log.d("CartActivity", "✅ Item selection updated, triggering UI refresh");
    } else {
      Log.w("CartActivity", "❌ No matching item found for auto-selection");
      Log.w("CartActivity", "   Expected: productId=" + autoSelectProductId + 
            ", variantId=" + autoSelectVariantId);
    }
    
    // Clear the auto-select flags so it doesn't trigger again
    autoSelectProductId = null;
    autoSelectVariantId = null;
    Log.d("CartActivity", "=== AUTO-SELECTION END ===");
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
    layoutSelectAll = findViewById(R.id.layoutSelectAll);
    checkboxSelectAll = findViewById(R.id.checkboxSelectAll);
    dividerSelectAll = findViewById(R.id.dividerSelectAll);
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
      if (cartManager.getUniqueSelectedItemCount() == 0) {
        Toast.makeText(this, "Vui lòng chọn sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
        return;
      }

      // Navigate to checkout activity with selected item IDs
      List<CartItem> selectedItems = cartManager.getSelectedItems();
      ArrayList<String> selectedItemIds = new ArrayList<>();
      for (CartItem item : selectedItems) {
        if (item.getId() != null) {
          selectedItemIds.add(item.getId());
        }
      }
      
      Intent checkoutIntent = new Intent(this, CheckoutActivity.class);
      checkoutIntent.putStringArrayListExtra("SELECTED_ITEM_IDS", selectedItemIds);
      Log.d("CartActivity", "Navigating to checkout with " + selectedItemIds.size() + " selected items");
      startActivity(checkoutIntent);
    });
    
    // Handle "Select All" checkbox
    checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
      if (buttonView.isPressed()) { // Only handle user clicks, not programmatic changes
        if (isChecked) {
          cartManager.selectAllItems();
        } else {
          cartManager.deselectAllItems();
        }
      }
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
      layoutSelectAll.setVisibility(View.GONE);
      dividerSelectAll.setVisibility(View.GONE);
      btnClearCart.setEnabled(false);
      btnCheckout.setEnabled(false);

      textTotalPrice.setText("₫0");
      textItemCount.setText("0 sản phẩm");
    } else {
      // Show cart items and select all header
      emptyCartLayout.setVisibility(View.GONE);
      recyclerViewCart.setVisibility(View.VISIBLE);
      layoutSelectAll.setVisibility(View.VISIBLE);
      dividerSelectAll.setVisibility(View.VISIBLE);
      btnClearCart.setEnabled(true);

      // Update adapter
      cartAdapter.updateCartItems(cartItems);

      // Calculate SELECTED items only
      double totalPriceSelected = cartManager.getTotalPriceOfSelected();
      int selectedCount = cartManager.getSelectedItemCount();
      int uniqueSelectedCount = cartManager.getUniqueSelectedItemCount();
      int totalUniqueCount = cartManager.getUniqueItemCount();

      // Update "Select All" checkbox state
      checkboxSelectAll.setOnCheckedChangeListener(null); // Remove listener temporarily
      checkboxSelectAll.setChecked(cartManager.isAllSelected());
      checkboxSelectAll.setText(String.format("Chọn tất cả (%d)", totalUniqueCount));
      checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
        if (buttonView.isPressed()) {
          if (isChecked) {
            cartManager.selectAllItems();
          } else {
            cartManager.deselectAllItems();
          }
        }
      });

      // Update price and count (selected items only)
      textTotalPrice.setText(String.format("₫%.0f", totalPriceSelected));

      if (uniqueSelectedCount > 0) {
        textItemCount.setText(String.format("Đã chọn %d sản phẩm (%d loại)",
            selectedCount, uniqueSelectedCount));
        btnCheckout.setEnabled(true);
      } else {
        textItemCount.setText(String.format("Chưa chọn sản phẩm nào (%d sản phẩm trong giỏ)",
            totalUniqueCount));
        btnCheckout.setEnabled(false);
      }
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

  @Override
  public void onItemSelectionChanged(CartItem item, boolean isSelected) {
    // Selection state is already updated in the item
    // Just trigger UI update
    cartManager.updateItemSelection(item.getId(), isSelected);
  }

  // CartManager.CartUpdateListener implementation
  @Override
  public void onCartUpdated(List<CartItem> cartItems) {
    Log.d("CartActivity", "onCartUpdated called with " + cartItems.size() + " items");
    runOnUiThread(() -> {
      updateUI(cartItems);
      
      // Handle auto-selection AFTER UI is updated (from "Buy Now" flow)
      if (isFirstLoad) {
        isFirstLoad = false;
        // Post to handler to ensure adapter has finished updating
        recyclerViewCart.post(() -> {
          handleAutoSelection();
        });
      }
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