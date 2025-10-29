package com.example.phoneshopapp.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.phoneshopapp.UserManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CartDebugHelper {
  private static final String TAG = "CartDebugHelper";
  private FirebaseFirestore db;
  private Context context;

  public CartDebugHelper(Context context) {
    this.context = context;
    this.db = FirebaseFirestore.getInstance();
  }

  // Kiểm tra và tạo dữ liệu cart đơn giản
  public void createSimpleCartItem() {
    UserManager userManager = UserManager.getInstance(context);
    if (!userManager.isLoggedIn()) {
      Toast.makeText(context, "Vui lòng đăng nhập trước", Toast.LENGTH_SHORT).show();
      return;
    }

    String userId = userManager.getCurrentUserId();
    Log.d(TAG, "Creating simple cart item for user: " + userId);

    // Tạo cart item đơn giản với dữ liệu cố định
    Map<String, Object> cartData = new HashMap<>();
    cartData.put("userId", userId);
    cartData.put("productId", 1); // int type
    cartData.put("productName", "Test Product");
    cartData.put("productPrice", "100,000 ₫");
    cartData.put("productPriceValue", 100000.0);
    cartData.put("productImageUrl", "");
    cartData.put("productImageResourceId", 0);
    cartData.put("productCategory", "Test");
    cartData.put("quantity", 1);
    cartData.put("addedAt", new Date());
    cartData.put("updatedAt", new Date());

    Log.d(TAG, "Cart data to be added: " + cartData.toString());

    db.collection("carts")
        .add(cartData)
        .addOnSuccessListener(documentReference -> {
          Log.d(TAG, "✅ Cart item created successfully with ID: " + documentReference.getId());
          Toast.makeText(context, "✅ Tạo cart item thành công! ID: " + documentReference.getId(), Toast.LENGTH_LONG)
              .show();
        })
        .addOnFailureListener(e -> {
          Log.e(TAG, "❌ Error creating cart item", e);
          Toast.makeText(context, "❌ Lỗi tạo cart item: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
  }

  // Kiểm tra quyền truy cập collection carts
  public void testCartsAccess() {
    UserManager userManager = UserManager.getInstance(context);
    if (!userManager.isLoggedIn()) {
      Toast.makeText(context, "Vui lòng đăng nhập trước", Toast.LENGTH_SHORT).show();
      return;
    }

    String userId = userManager.getCurrentUserId();
    Log.d(TAG, "Testing carts collection access for user: " + userId);

    db.collection("carts")
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener(querySnapshot -> {
          Log.d(TAG, "✅ Successfully accessed carts collection");
          Log.d(TAG, "Found " + querySnapshot.size() + " documents");
          Toast.makeText(context, "✅ Truy cập carts thành công! Tìm thấy " + querySnapshot.size() + " items",
              Toast.LENGTH_LONG).show();
        })
        .addOnFailureListener(e -> {
          Log.e(TAG, "❌ Failed to access carts collection", e);
          Toast.makeText(context, "❌ Lỗi truy cập carts: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
  }

  // Xóa tất cả cart items của user hiện tại
  public void clearCurrentUserCart() {
    UserManager userManager = UserManager.getInstance(context);
    if (!userManager.isLoggedIn()) {
      return;
    }

    String userId = userManager.getCurrentUserId();
    Log.d(TAG, "Clearing cart for user: " + userId);

    db.collection("carts")
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener(querySnapshot -> {
          int count = querySnapshot.size();
          for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot) {
            document.getReference().delete();
          }
          Log.d(TAG, "Cleared " + count + " cart items");
          Toast.makeText(context, "Đã xóa " + count + " items khỏi cart", Toast.LENGTH_SHORT).show();
        })
        .addOnFailureListener(e -> {
          Log.e(TAG, "Error clearing cart", e);
          Toast.makeText(context, "Lỗi xóa cart: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
  }

  // Test Firebase connection
  public void testFirebaseConnection() {
    Log.d(TAG, "Testing Firebase connection...");

    db.collection("test")
        .add(Map.of("timestamp", new Date(), "test", true))
        .addOnSuccessListener(documentReference -> {
          Log.d(TAG, "✅ Firebase connection OK");
          Toast.makeText(context, "✅ Firebase kết nối thành công", Toast.LENGTH_SHORT).show();
          // Clean up test document
          documentReference.delete();
        })
        .addOnFailureListener(e -> {
          Log.e(TAG, "❌ Firebase connection failed", e);
          Toast.makeText(context, "❌ Firebase lỗi kết nối: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
  }
}