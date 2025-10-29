package com.example.phoneshopapp.data.variant;

import android.util.Log;
import com.example.phoneshopapp.models.ProductVariant;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing product variants in Firestore
 */
public class VariantRepository {
  private static final String TAG = "VariantRepository";
  private static final String COLLECTION_VARIANTS = "product_variants";

  private final FirebaseFirestore firestore;

  public VariantRepository() {
    this.firestore = FirebaseFirestore.getInstance();
  }

  // Callback interface for loading variants
  public interface OnVariantsLoadedListener {
    void onSuccess(List<ProductVariant> variants);

    void onFailure(Exception e);
  }

  /**
   * Load all variants for a specific product
   * 
   * @param productId The product ID to load variants for
   * @param listener  Callback listener
   */
  public void loadVariantsByProductId(String productId, OnVariantsLoadedListener listener) {
    Log.d(TAG, "Loading variants for product: " + productId);

    firestore.collection(COLLECTION_VARIANTS)
        .whereEqualTo("productId", productId)
        .get()
        .addOnSuccessListener(queryDocumentSnapshots -> {
          List<ProductVariant> variants = new ArrayList<>();

          for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
            try {
              ProductVariant variant = documentToVariant(document);
              if (variant != null) {
                variants.add(variant);
                Log.d(TAG, "Loaded variant: " + variant.getShortName());
              }
            } catch (Exception e) {
              Log.e(TAG, "Error parsing variant document: " + document.getId(), e);
            }
          }

          Log.d(TAG, "Successfully loaded " + variants.size() + " variants for product: " + productId);
          listener.onSuccess(variants);
        })
        .addOnFailureListener(e -> {
          Log.e(TAG, "Failed to load variants for product: " + productId, e);
          listener.onFailure(e);
        });
  }

  /**
   * Convert Firestore document to ProductVariant object
   * Handles nested structure: attributes, display, inventory
   */
  private ProductVariant documentToVariant(QueryDocumentSnapshot document) {
    ProductVariant variant = new ProductVariant();

    // Set document ID as variantId
    variant.setVariantId(document.getId());

    // Main fields
    variant.setProductId(document.getString("productId"));

    // Attributes group (nested map)
    Object attributesObj = document.get("attributes");
    if (attributesObj instanceof java.util.Map) {
      @SuppressWarnings("unchecked")
      java.util.Map<String, Object> attributes = (java.util.Map<String, Object>) attributesObj;
      variant.setColor((String) attributes.get("color"));
      variant.setColorHex((String) attributes.get("colorHex"));
      variant.setRam((String) attributes.get("ram"));
      variant.setStorage((String) attributes.get("storage"));
    }

    // Display group (nested map)
    Object displayObj = document.get("display");
    if (displayObj instanceof java.util.Map) {
      @SuppressWarnings("unchecked")
      java.util.Map<String, Object> display = (java.util.Map<String, Object>) displayObj;
      variant.setName((String) display.get("name"));
      variant.setShortName((String) display.get("shortName"));
    }

    // Inventory group (nested map)
    Object inventoryObj = document.get("inventory");
    if (inventoryObj instanceof java.util.Map) {
      @SuppressWarnings("unchecked")
      java.util.Map<String, Object> inventory = (java.util.Map<String, Object>) inventoryObj;

      Boolean isAvailable = (Boolean) inventory.get("isAvailable");
      variant.setAvailable(isAvailable != null ? isAvailable : false);

      variant.setSku((String) inventory.get("sku"));

      Object stockObj = inventory.get("stockQuantity");
      if (stockObj instanceof Long) {
        variant.setStockQuantity(((Long) stockObj).intValue());
      } else if (stockObj instanceof Integer) {
        variant.setStockQuantity((Integer) stockObj);
      } else {
        variant.setStockQuantity(0);
      }
    }

    return variant;
  }
}
