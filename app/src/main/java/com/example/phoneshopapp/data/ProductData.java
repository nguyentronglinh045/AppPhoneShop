package com.example.phoneshopapp.data;

import android.util.Log;
import com.example.phoneshopapp.Product;
import com.example.phoneshopapp.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ProductData {

  private static final String TAG = "ProductData";
  private static final String FIREBASE_COLLECTION = "PhoneDB";

  // Static list để giữ dữ liệu giữa các lần gọi
  private static List<Product> productList = null;
  private static boolean isLoadingFromFirebase = false;
  private static long lastFirebaseLoadTime = 0;
  private static final long FIREBASE_CACHE_DURATION = 1 * 60 * 1000; // 1 phút cache Firebase data để data luôn mới

  // Interface cho callback khi load data từ Firebase
  public interface OnProductsLoadedListener {
    void onSuccess(List<Product> products);

    void onFailure(Exception e);
  }

  // Kiểm tra xem có nên load lại từ Firebase không
  private static boolean shouldReloadFromFirebase() {
    return productList == null ||
        (System.currentTimeMillis() - lastFirebaseLoadTime) > FIREBASE_CACHE_DURATION;
  }

  // Load products từ Firebase
  public static void loadProductsFromFirebase(OnProductsLoadedListener listener) {
    // Nếu đang loading thì đợi
    if (isLoadingFromFirebase) {
      Log.d(TAG, "Already loading from Firebase, please wait...");
      return;
    }

    // Nếu có cache và chưa hết hạn thì dùng cache
    if (!shouldReloadFromFirebase() && productList != null && !productList.isEmpty()) {
      Log.d(TAG, "Using cached Firebase data (" + productList.size() + " products)");
      listener.onSuccess(new ArrayList<>(productList));
      return;
    }

    isLoadingFromFirebase = true;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    Log.d(TAG, "Loading products from Firebase PhoneDB collection...");
    Log.d(TAG, "Firebase instance: " + db.toString());

    db.collection(FIREBASE_COLLECTION)
        .get()
        .addOnSuccessListener(queryDocumentSnapshots -> {
          List<Product> firebaseProducts = new ArrayList<>();

          Log.d(TAG, "Firebase query successful. Documents count: " + queryDocumentSnapshots.size());

          for (DocumentSnapshot doc : queryDocumentSnapshots) {
            try {
              // Log raw data from Firebase for debugging
              Log.d(TAG, "Raw Firebase document ID: " + doc.getId());
              Log.d(TAG, "Raw Firebase data: " + doc.getData());

              Product product = documentToProduct(doc);
              if (product != null) {
                firebaseProducts.add(product);
                Log.d(TAG, "Loaded product: ID=" + product.getId() + ", Name=" + product.getName() +
                    ", DocID=" + doc.getId() + ", Brand=" + product.getBrand() +
                    ", HasVariants=" + product.isHasVariants());
              } else {
                Log.e(TAG, "Failed to convert document to Product object. Document ID: " + doc.getId());
              }
            } catch (Exception e) {
              Log.e(TAG, "Error parsing document " + doc.getId(), e);
              Log.e(TAG, "Document data: " + doc.getData());
            }
          }

          // Cập nhật static list với dữ liệu từ Firebase
          productList = firebaseProducts;
          lastFirebaseLoadTime = System.currentTimeMillis();
          isLoadingFromFirebase = false;

          Log.d(TAG, "Successfully loaded " + firebaseProducts.size() + " products from Firebase");

          // Chỉ dùng dữ liệu Firebase - nếu Firebase trống thì báo lỗi
          if (firebaseProducts.isEmpty()) {
            Log.w(TAG, "No products found in Firebase database");
            listener.onFailure(new Exception("Không tìm thấy sản phẩm nào trên hệ thống. Vui lòng thử lại sau."));
          } else {
            listener.onSuccess(new ArrayList<>(firebaseProducts));
          }
        })
        .addOnFailureListener(e -> {
          Log.e(TAG, "Error loading products from Firebase", e);
          isLoadingFromFirebase = false;

          // Chỉ trả về lỗi thực tế, không có fallback
          String errorMessage = "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng và thử lại.";
          if (e.getMessage() != null && e.getMessage().contains("offline")) {
            errorMessage = "Thiết bị đang offline. Vui lòng kết nối mạng và thử lại.";
          }

          listener.onFailure(new Exception(errorMessage));
        });
  }

  // Clear cache để force reload từ Firebase
  public static void clearCache() {
    productList = null;
    lastFirebaseLoadTime = 0;
    isLoadingFromFirebase = false;
    Log.d(TAG, "ProductData cache cleared");
  }

  // Force refresh từ Firebase
  public static void forceRefreshFromFirebase(OnProductsLoadedListener listener) {
    clearCache();
    loadProductsFromFirebase(listener);
  }

  // Helper method to convert Firestore document to Product object
  private static Product documentToProduct(DocumentSnapshot doc) {
    try {
      Product product = new Product();

      // Set document ID as product ID
      String documentId = doc.getId();
      product.setId(documentId);

      // Set all other fields from document
      product.setName(doc.getString("name"));
      product.setPrice(doc.getString("price"));
      product.setImageUrl(doc.getString("imageUrl"));
      product.setDescription(doc.getString("description"));
      product.setCategory(doc.getString("category"));
      product.setBrand(doc.getString("brand"));
      product.setSpecScreen(doc.getString("specScreen"));
      product.setSpecProcessor(doc.getString("specProcessor"));
      product.setSpecRam(doc.getString("specRam"));
      product.setSpecStorage(doc.getString("specStorage"));

      // Handle nullable boolean fields
      Boolean isFeatured = doc.getBoolean("isFeatured");
      if (isFeatured != null) {
        product.setFeatured(isFeatured);
      }

      Boolean isBestDeal = doc.getBoolean("isBestDeal");
      if (isBestDeal != null) {
        product.setBestDeal(isBestDeal);
      }

      Boolean hasVariants = doc.getBoolean("hasVariants");
      if (hasVariants != null) {
        product.setHasVariants(hasVariants);
      }

      // Handle numeric fields
      Long stockQuantity = doc.getLong("stockQuantity");
      if (stockQuantity != null) {
        product.setStockQuantity(stockQuantity.intValue());
      }

      Long imageResourceId = doc.getLong("imageResourceId");
      if (imageResourceId != null) {
        product.setImageResourceId(imageResourceId.intValue());
      }

      // Calculate priceValue from price string
      product.setPriceValue(parsePrice(product.getPrice()));

      return product;
    } catch (Exception e) {
      Log.e(TAG, "Error converting document to Product: " + doc.getId(), e);
      return null;
    }
  }

  // Helper method để parse price string thành number (replicate from
  // Product.java)
  private static double parsePrice(String price) {
    if (price == null)
      return 0.0;
    try {
      String cleanPrice = price.replaceAll("[^\\d.]", "");
      return Double.parseDouble(cleanPrice);
    } catch (NumberFormatException e) {
      return 0.0;
    }
  }
}