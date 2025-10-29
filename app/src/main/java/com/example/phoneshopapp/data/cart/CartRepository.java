package com.example.phoneshopapp.data.cart;

import android.util.Log;
import com.example.phoneshopapp.models.CartItem;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartRepository {
  private static final String COLLECTION_CARTS = "carts";
  private final FirebaseFirestore db;

  public CartRepository() {
    this.db = FirebaseFirestore.getInstance();
  }

  // Interface cho callback
  public interface OnCartOperationListener {
    void onSuccess();

    void onFailure(Exception e);
  }

  public interface OnCartItemsLoadedListener {
    void onSuccess(List<CartItem> cartItems);

    void onFailure(Exception e);
  }

  public interface OnCartItemLoadedListener {
    void onSuccess(CartItem cartItem);

    void onFailure(Exception e);
  }

  public interface OnCartCountListener {
    void onSuccess(int count);

    void onFailure(Exception e);
  }

  // Thêm sản phẩm vào giỏ hàng
  public void addToCart(String userId, CartItem cartItem, OnCartOperationListener listener) {
    Log.d("CartRepository", "Adding to cart for userId: " + userId + ", product: " + cartItem.getProductName());
    if (cartItem.getVariantId() != null) {
      Log.d("CartRepository", "With variant: " + cartItem.getVariantShortName());
    }

    // Kiểm tra xem sản phẩm (với variant cụ thể) đã có trong cart chưa
    checkIfProductExistsInCart(userId, cartItem.getProductId(), cartItem.getVariantId(),
        new OnCartItemLoadedListener() {
          @Override
          public void onSuccess(CartItem existingItem) {
            if (existingItem != null) {
              Log.d("CartRepository", "Product+Variant exists, updating quantity");
              // Cập nhật quantity nếu sản phẩm với variant đã tồn tại
              updateCartItemQuantity(existingItem.getId(),
                  existingItem.getQuantity() + cartItem.getQuantity(), listener);
            } else {
              Log.d("CartRepository", "Product+Variant new, adding to cart");
              // Thêm mới nếu chưa có
              addNewCartItem(cartItem, listener);
            }
          }

          @Override
          public void onFailure(Exception e) {
            Log.w("CartRepository", "Failed to check existing item, adding new anyway", e);
            // Nếu có lỗi khi check, vẫn thử thêm mới
            addNewCartItem(cartItem, listener);
          }
        });
  }

  // Thêm sản phẩm mới vào cart
  private void addNewCartItem(CartItem cartItem, OnCartOperationListener listener) {
    Log.d("CartRepository",
        "Adding new cart item: " + cartItem.getProductName() + " (quantity: " + cartItem.getQuantity() + ")");

    Map<String, Object> cartData = new HashMap<>();
    cartData.put("userId", cartItem.getUserId());
    cartData.put("productId", cartItem.getProductId());
    cartData.put("productName", cartItem.getProductName());
    cartData.put("productPrice", cartItem.getProductPrice());
    cartData.put("productPriceValue", cartItem.getProductPriceValue());
    cartData.put("productImageUrl", cartItem.getProductImageUrl());
    cartData.put("productImageResourceId", cartItem.getProductImageResourceId());
    cartData.put("productCategory", cartItem.getProductCategory());
    cartData.put("quantity", cartItem.getQuantity());
    cartData.put("addedAt", cartItem.getAddedAt());
    cartData.put("updatedAt", cartItem.getUpdatedAt());

    // Add variant information if present
    if (cartItem.getVariantId() != null) {
      cartData.put("variantId", cartItem.getVariantId());
      cartData.put("variantName", cartItem.getVariantName());
      cartData.put("variantShortName", cartItem.getVariantShortName());
      cartData.put("variantColor", cartItem.getVariantColor());
      cartData.put("variantColorHex", cartItem.getVariantColorHex());
      cartData.put("variantRam", cartItem.getVariantRam());
      cartData.put("variantStorage", cartItem.getVariantStorage());
      Log.d("CartRepository", "Adding variant: " + cartItem.getVariantShortName());
    }

    db.collection(COLLECTION_CARTS)
        .add(cartData)
        .addOnSuccessListener(documentReference -> {
          Log.d("CartRepository", "Cart item added with ID: " + documentReference.getId());
          if (listener != null) {
            listener.onSuccess();
          }
        })
        .addOnFailureListener(e -> {
          Log.e("CartRepository", "Error adding cart item to Firestore", e);
          if (listener != null) {
            listener.onFailure(e);
          }
        });
  }

  // Kiểm tra sản phẩm (với variant cụ thể) đã có trong cart chưa
  // Different variants of same product are treated as different items
  private void checkIfProductExistsInCart(String userId, String productId, String variantId,
      OnCartItemLoadedListener listener) {
    Query query = db.collection(COLLECTION_CARTS)
        .whereEqualTo("userId", userId)
        .whereEqualTo("productId", productId);

    // Add variantId to query if present
    if (variantId != null) {
      query = query.whereEqualTo("variantId", variantId);
    } else {
      // For products without variants, we need to check that variantId is null
      // This is more complex in Firestore, so we'll filter in code
    }

    query.limit(10) // Get up to 10 to filter in code if needed
        .get()
        .addOnSuccessListener(querySnapshot -> {
          CartItem matchingItem = null;

          for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            CartItem cartItem = document.toObject(CartItem.class);
            if (cartItem != null) {
              cartItem.setId(document.getId());

              // Check if variantId matches (both null or both equal)
              if (variantId == null && cartItem.getVariantId() == null) {
                matchingItem = cartItem;
                break;
              } else if (variantId != null && variantId.equals(cartItem.getVariantId())) {
                matchingItem = cartItem;
                break;
              }
            }
          }

          listener.onSuccess(matchingItem);
        })
        .addOnFailureListener(listener::onFailure);
  }

  // Lấy tất cả sản phẩm trong giỏ hàng của user
  public void getCartItems(String userId, OnCartItemsLoadedListener listener) {
    Log.d("CartRepository", "Fetching cart items for userId: " + userId);
    Log.d("CartRepository", "Collection: " + COLLECTION_CARTS);

    // Tạm thời bỏ orderBy để tránh lỗi index, sẽ sort trong code
    db.collection(COLLECTION_CARTS)
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener(querySnapshot -> {
          Log.d("CartRepository", "Query successful. Documents found: " + querySnapshot.size());
          List<CartItem> cartItems = new ArrayList<>();
          for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            Log.d("CartRepository", "Processing document: " + document.getId());
            CartItem cartItem = document.toObject(CartItem.class);
            if (cartItem != null) {
              cartItem.setId(document.getId());

              // Debug log variant info
              if (cartItem.getVariantId() != null) {
                Log.d("CartRepository", "Variant detected for " + cartItem.getProductName() +
                    ": " + cartItem.getVariantShortName());
              } else {
                Log.d("CartRepository", "No variant for " + cartItem.getProductName());
              }

              cartItems.add(cartItem);
              Log.d("CartRepository", "Added cart item: " + cartItem.getProductName());
            }
          }

          // Sort in memory by addedAt (newest first)
          cartItems.sort((item1, item2) -> {
            if (item1.getAddedAt() == null && item2.getAddedAt() == null)
              return 0;
            if (item1.getAddedAt() == null)
              return 1;
            if (item2.getAddedAt() == null)
              return -1;
            return item2.getAddedAt().compareTo(item1.getAddedAt());
          });

          Log.d("CartRepository", "Total cart items loaded: " + cartItems.size());
          listener.onSuccess(cartItems);
        })
        .addOnFailureListener(e -> {
          Log.e("CartRepository", "Failed to fetch cart items for userId: " + userId, e);
          Log.e("CartRepository", "Error type: " + e.getClass().getSimpleName());
          Log.e("CartRepository", "Error message: " + e.getMessage());
          listener.onFailure(e);
        });
  }

  // Cập nhật số lượng sản phẩm trong giỏ hàng
  public void updateCartItemQuantity(String cartItemId, int newQuantity, OnCartOperationListener listener) {
    Log.d("CartRepository", "Updating cart item quantity: " + cartItemId + " to quantity: " + newQuantity);

    if (newQuantity <= 0) {
      Log.d("CartRepository", "Quantity <= 0, removing item instead");
      removeCartItem(cartItemId, listener);
      return;
    }

    Map<String, Object> updates = new HashMap<>();
    updates.put("quantity", newQuantity);
    updates.put("updatedAt", new Date());

    db.collection(COLLECTION_CARTS)
        .document(cartItemId)
        .update(updates)
        .addOnSuccessListener(aVoid -> {
          Log.d("CartRepository", "Cart item quantity updated successfully");
          if (listener != null) {
            listener.onSuccess();
          }
        })
        .addOnFailureListener(e -> {
          Log.e("CartRepository", "Error updating cart item quantity", e);
          if (listener != null) {
            listener.onFailure(e);
          }
        });
  }

  // Xóa sản phẩm khỏi giỏ hàng
  public void removeCartItem(String cartItemId, OnCartOperationListener listener) {
    Log.d("CartRepository", "Removing cart item: " + cartItemId);

    db.collection(COLLECTION_CARTS)
        .document(cartItemId)
        .delete()
        .addOnSuccessListener(aVoid -> {
          Log.d("CartRepository", "Cart item removed successfully");
          if (listener != null) {
            listener.onSuccess();
          }
        })
        .addOnFailureListener(e -> {
          Log.e("CartRepository", "Error removing cart item", e);
          if (listener != null) {
            listener.onFailure(e);
          }
        });
  }

  // Xóa tất cả sản phẩm trong giỏ hàng của user
  public void clearCart(String userId, OnCartOperationListener listener) {
    db.collection(COLLECTION_CARTS)
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener(querySnapshot -> {
          if (querySnapshot.isEmpty()) {
            if (listener != null) {
              listener.onSuccess();
            }
            return;
          }

          // Xóa tất cả documents
          List<Task<Void>> deleteTasks = new ArrayList<>();
          for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            deleteTasks.add(document.getReference().delete());
          }

          Tasks.whenAll(deleteTasks)
              .addOnSuccessListener(aVoid -> {
                if (listener != null) {
                  listener.onSuccess();
                }
              })
              .addOnFailureListener(e -> {
                if (listener != null) {
                  listener.onFailure(e);
                }
              });
        })
        .addOnFailureListener(e -> {
          if (listener != null) {
            listener.onFailure(e);
          }
        });
  }

  // Đếm số lượng sản phẩm trong giỏ hàng
  public void getCartItemCount(String userId, OnCartCountListener listener) {
    db.collection(COLLECTION_CARTS)
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener(querySnapshot -> {
          int totalCount = 0;
          for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            CartItem cartItem = document.toObject(CartItem.class);
            if (cartItem != null) {
              totalCount += cartItem.getQuantity();
            }
          }
          listener.onSuccess(totalCount);
        })
        .addOnFailureListener(listener::onFailure);
  }

  // Tính tổng giá trị giỏ hàng
  public void getCartTotalValue(String userId, OnCartCountListener listener) {
    db.collection(COLLECTION_CARTS)
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener(querySnapshot -> {
          double totalValue = 0.0;
          for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            CartItem cartItem = document.toObject(CartItem.class);
            if (cartItem != null) {
              totalValue += cartItem.getTotalPrice();
            }
          }
          listener.onSuccess((int) totalValue);
        })
        .addOnFailureListener(listener::onFailure);
  }
}