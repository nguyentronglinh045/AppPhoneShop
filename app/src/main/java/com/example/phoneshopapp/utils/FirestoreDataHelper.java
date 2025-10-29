package com.example.phoneshopapp.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.phoneshopapp.UserManager;
import com.example.phoneshopapp.models.CartItem;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirestoreDataHelper {
  private static final String TAG = "FirestoreDataHelper";
  private FirebaseFirestore db;
  private Context context;

  public FirestoreDataHelper(Context context) {
    this.context = context;
    this.db = FirebaseFirestore.getInstance();
  }

  // Tạo sample cart data để test
  public void createSampleCartData() {
    UserManager userManager = UserManager.getInstance(context);
    if (!userManager.isLoggedIn()) {
      Toast.makeText(context, "Vui lòng đăng nhập trước", Toast.LENGTH_SHORT).show();
      return;
    }

    String userId = userManager.getCurrentUserId();
    Log.d(TAG, "Creating sample cart data for user: " + userId);

    // Sample cart item 1
    createSampleCartItem(userId, "1", "iPhone 15 Pro", "25,990,000 ₫", 25990000,
        "https://example.com/iphone15.jpg", 0, "Smartphones", 1);

    // Sample cart item 2
    createSampleCartItem(userId, "2", "Samsung Galaxy S24", "22,990,000 ₫", 22990000,
        "https://example.com/galaxy24.jpg", 0, "Smartphones", 2);

    Toast.makeText(context, "Đã tạo dữ liệu giỏ hàng mẫu", Toast.LENGTH_LONG).show();
  }

  private void createSampleCartItem(String userId, String productId, String productName,
      String productPrice, long productPriceValue,
      String imageUrl, int imageResourceId,
      String category, int quantity) {

    Map<String, Object> cartData = new HashMap<>();
    cartData.put("userId", userId);
    cartData.put("productId", productId);
    cartData.put("productName", productName);
    cartData.put("productPrice", productPrice);
    cartData.put("productPriceValue", productPriceValue);
    cartData.put("productImageUrl", imageUrl);
    cartData.put("productImageResourceId", imageResourceId);
    cartData.put("productCategory", category);
    cartData.put("quantity", quantity);
    cartData.put("addedAt", new Date());
    cartData.put("updatedAt", new Date());

    db.collection("carts")
        .add(cartData)
        .addOnSuccessListener(documentReference -> {
          Log.d(TAG, "Sample cart item created with ID: " + documentReference.getId());
        })
        .addOnFailureListener(e -> {
          Log.e(TAG, "Error creating sample cart item", e);
          Toast.makeText(context, "Lỗi tạo dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
  }

  // Xóa tất cả cart data của user hiện tại
  public void clearUserCartData() {
    UserManager userManager = UserManager.getInstance(context);
    if (!userManager.isLoggedIn()) {
      return;
    }

    String userId = userManager.getCurrentUserId();
    Log.d(TAG, "Clearing cart data for user: " + userId);

    db.collection("carts")
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener(queryDocumentSnapshots -> {
          for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
            document.getReference().delete();
          }
          Log.d(TAG, "Cleared " + queryDocumentSnapshots.size() + " cart items");
          Toast.makeText(context, "Đã xóa giỏ hàng", Toast.LENGTH_SHORT).show();
        })
        .addOnFailureListener(e -> {
          Log.e(TAG, "Error clearing cart data", e);
          Toast.makeText(context, "Lỗi xóa giỏ hàng: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
  }

  // Kiểm tra và tạo collection "carts" nếu chưa tồn tại
  public void ensureCartsCollectionExists() {
    Log.d(TAG, "Checking if carts collection exists...");

    db.collection("carts")
        .limit(1)
        .get()
        .addOnSuccessListener(queryDocumentSnapshots -> {
          if (queryDocumentSnapshots.isEmpty()) {
            Log.d(TAG, "Carts collection is empty, will be created when first item is added");
            Toast.makeText(context, "Collection 'carts' sẽ được tạo khi thêm item đầu tiên", Toast.LENGTH_SHORT).show();
          } else {
            Log.d(TAG, "Carts collection exists with " + queryDocumentSnapshots.size() + " items");
            Toast.makeText(context, "Collection 'carts' đã tồn tại", Toast.LENGTH_SHORT).show();
          }
        })
        .addOnFailureListener(e -> {
          Log.e(TAG, "Error checking carts collection", e);
          Toast.makeText(context, "Lỗi kiểm tra collection: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
  }
}