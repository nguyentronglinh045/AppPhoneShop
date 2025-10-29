package com.example.phoneshopapp.data.cart;

import android.content.Context;
import android.util.Log;
import com.example.phoneshopapp.Product;
import com.example.phoneshopapp.UserManager;
import com.example.phoneshopapp.models.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
  private static final String TAG = "CartManager";
  private static CartManager instance;
  private CartRepository cartRepository;
  private UserManager userManager;
  private List<CartItem> cartItems;
  private List<CartUpdateListener> listeners;

  // Interface cho các listener
  public interface CartUpdateListener {
    void onCartUpdated(List<CartItem> cartItems);

    void onCartCountChanged(int count);

    void onCartError(String message);
  }

  public interface OnCartOperationListener {
    void onSuccess(String message);

    void onFailure(String error);
  }

  private CartManager() {
    this.cartRepository = new CartRepository();
    this.cartItems = new ArrayList<>();
    this.listeners = new ArrayList<>();
  }

  public static synchronized CartManager getInstance() {
    if (instance == null) {
      instance = new CartManager();
    }
    return instance;
  }

  public void initialize(Context context) {
    this.userManager = UserManager.getInstance(context);
    loadCartItems();
  }

  // Thêm listener
  public void addCartUpdateListener(CartUpdateListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  // Xóa listener
  public void removeCartUpdateListener(CartUpdateListener listener) {
    listeners.remove(listener);
  }

  // Notify all listeners
  private void notifyCartUpdated() {
    for (CartUpdateListener listener : listeners) {
      listener.onCartUpdated(new ArrayList<>(cartItems));
      listener.onCartCountChanged(getTotalItemCount());
    }
  }

  private void notifyCartError(String message) {
    for (CartUpdateListener listener : listeners) {
      listener.onCartError(message);
    }
  }

  // Load cart items from Firestore
  public void loadCartItems() {
    if (userManager == null) {
      Log.w(TAG, "UserManager is null, cannot load cart");
      notifyCartError("Lỗi hệ thống: UserManager chưa được khởi tạo");
      return;
    }

    if (!userManager.isLoggedIn()) {
      Log.w(TAG, "User not logged in, cannot load cart");
      // Clear local cart when user is not logged in
      cartItems.clear();
      notifyCartUpdated();
      return;
    }

    String userId = userManager.getCurrentUserId();
    if (userId == null) {
      Log.w(TAG, "User ID is null, cannot load cart");
      notifyCartError("Không thể xác định người dùng. Vui lòng đăng nhập lại.");
      return;
    }

    Log.d(TAG, "Loading cart for user: " + userId);

    cartRepository.getCartItems(userId, new CartRepository.OnCartItemsLoadedListener() {
      @Override
      public void onSuccess(List<CartItem> items) {
        cartItems.clear();
        cartItems.addAll(items);
        notifyCartUpdated();
        Log.d(TAG, "Loaded " + items.size() + " cart items for user: " + userId);
      }

      @Override
      public void onFailure(Exception e) {
        Log.e(TAG, "Failed to load cart items for user: " + userId, e);
        notifyCartError("Không thể tải giỏ hàng: " + e.getMessage());
      }
    });
  }

  // Thêm sản phẩm vào giỏ hàng
  public void addToCart(Product product, int quantity, com.example.phoneshopapp.models.ProductVariant variant,
      OnCartOperationListener listener) {
    Log.d(TAG, "=== ADD TO CART DEBUG START ===");
    Log.d(TAG, "Product: " + (product != null ? product.getName() : "null"));
    Log.d(TAG, "Quantity: " + quantity);
    Log.d(TAG, "Variant: " + (variant != null ? variant.getShortName() : "null"));

    if (userManager == null || !userManager.isLoggedIn()) {
      Log.e(TAG, "User not logged in");
      if (listener != null) {
        listener.onFailure("Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng");
      }
      return;
    }

    String userId = userManager.getCurrentUserId();
    Log.d(TAG, "User ID: " + userId);

    if (userId == null) {
      Log.e(TAG, "User ID is null");
      if (listener != null) {
        listener.onFailure("Không thể xác định người dùng");
      }
      return;
    }

    Log.d(TAG, "Creating CartItem...");
    CartItem cartItem = new CartItem(userId, product, quantity, variant);
    Log.d(TAG, "CartItem created: " + cartItem.getProductName());

    cartRepository.addToCart(userId, cartItem, new CartRepository.OnCartOperationListener() {
      @Override
      public void onSuccess() {
        Log.d(TAG, "Add to cart SUCCESS");
        loadCartItems(); // Reload để cập nhật UI
        if (listener != null) {
          listener.onSuccess("Đã thêm " + quantity + " " + product.getName() + " vào giỏ hàng");
        }
        Log.d(TAG, "Added to cart: " + product.getName() + " x" + quantity);
      }

      @Override
      public void onFailure(Exception e) {
        Log.e(TAG, "Add to cart FAILED: " + e.getMessage(), e);
        if (listener != null) {
          listener.onFailure("Không thể thêm vào giỏ hàng: " + e.getMessage());
        }
        Log.e(TAG, "Failed to add to cart", e);
      }
    });
  }

  // Cập nhật số lượng sản phẩm
  public void updateCartItemQuantity(String cartItemId, int newQuantity, OnCartOperationListener listener) {
    cartRepository.updateCartItemQuantity(cartItemId, newQuantity, new CartRepository.OnCartOperationListener() {
      @Override
      public void onSuccess() {
        loadCartItems(); // Reload để cập nhật UI
        if (listener != null) {
          listener.onSuccess("Đã cập nhật số lượng");
        }
      }

      @Override
      public void onFailure(Exception e) {
        if (listener != null) {
          listener.onFailure("Không thể cập nhật: " + e.getMessage());
        }
        Log.e(TAG, "Failed to update quantity", e);
      }
    });
  }

  // Xóa sản phẩm khỏi giỏ hàng
  public void removeFromCart(String cartItemId, OnCartOperationListener listener) {
    cartRepository.removeCartItem(cartItemId, new CartRepository.OnCartOperationListener() {
      @Override
      public void onSuccess() {
        loadCartItems(); // Reload để cập nhật UI
        if (listener != null) {
          listener.onSuccess("Đã xóa sản phẩm khỏi giỏ hàng");
        }
      }

      @Override
      public void onFailure(Exception e) {
        if (listener != null) {
          listener.onFailure("Không thể xóa: " + e.getMessage());
        }
        Log.e(TAG, "Failed to remove from cart", e);
      }
    });
  }

  // Xóa tất cả sản phẩm trong giỏ hàng
  public void clearCart(OnCartOperationListener listener) {
    if (userManager == null || !userManager.isLoggedIn()) {
      if (listener != null) {
        listener.onFailure("Người dùng chưa đăng nhập");
      }
      return;
    }

    String userId = userManager.getCurrentUserId();
    cartRepository.clearCart(userId, new CartRepository.OnCartOperationListener() {
      @Override
      public void onSuccess() {
        cartItems.clear();
        notifyCartUpdated();
        if (listener != null) {
          listener.onSuccess("Đã xóa tất cả sản phẩm trong giỏ hàng");
        }
      }

      @Override
      public void onFailure(Exception e) {
        if (listener != null) {
          listener.onFailure("Không thể xóa giỏ hàng: " + e.getMessage());
        }
        Log.e(TAG, "Failed to clear cart", e);
      }
    });
  }

  // Getter methods
  public List<CartItem> getCartItems() {
    return new ArrayList<>(cartItems);
  }

  public int getTotalItemCount() {
    int count = 0;
    for (CartItem item : cartItems) {
      count += item.getQuantity();
    }
    return count;
  }

  public double getTotalPrice() {
    double total = 0.0;
    for (CartItem item : cartItems) {
      total += item.getTotalPrice();
    }
    return total;
  }

  public boolean isEmpty() {
    return cartItems.isEmpty();
  }

  public int getUniqueItemCount() {
    return cartItems.size();
  }

  // Kiểm tra sản phẩm đã có trong cart chưa
  public boolean containsProduct(String productId) {
    for (CartItem item : cartItems) {
      if (item.getProductId() != null && item.getProductId().equals(productId)) {
        return true;
      }
    }
    return false;
  }

  // Lấy CartItem theo productId
  public CartItem getCartItemByProductId(String productId) {
    for (CartItem item : cartItems) {
      if (item.getProductId() != null && item.getProductId().equals(productId)) {
        return item;
      }
    }
    return null;
  }

  // Refresh cart từ server
  public void refreshCart() {
    loadCartItems();
  }

  // Format currency
  public String formatPrice(double price) {
    return String.format("$%.0f", price);
  }

  public String getTotalPriceFormatted() {
    return formatPrice(getTotalPrice());
  }
}