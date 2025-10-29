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

  // Callback interface for saving variant
  public interface OnVariantSavedListener {
    void onSuccess();

    void onFailure(Exception e);
  }

  // Callback interface for deleting variant
  public interface OnVariantDeletedListener {
    void onSuccess();

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

  /**
   * Create a new variant in Firestore
   * 
   * @param variant  The variant to create
   * @param listener Callback listener
   */
  public void createVariant(ProductVariant variant, OnVariantSavedListener listener) {
    // Generate variant ID if not set
    if (variant.getVariantId() == null || variant.getVariantId().isEmpty()) {
      variant.setVariantId(generateVariantId(variant.getProductId()));
    }

    // Convert variant to Firestore map
    java.util.Map<String, Object> variantMap = variantToMap(variant);

    firestore.collection(COLLECTION_VARIANTS)
        .document(variant.getVariantId())
        .set(variantMap)
        .addOnSuccessListener(aVoid -> {
          Log.d(TAG, "Variant created successfully: " + variant.getVariantId());
          listener.onSuccess();
        })
        .addOnFailureListener(e -> {
          Log.e(TAG, "Failed to create variant", e);
          listener.onFailure(e);
        });
  }

  /**
   * Update an existing variant in Firestore
   * 
   * @param variant  The variant to update
   * @param listener Callback listener
   */
  public void updateVariant(ProductVariant variant, OnVariantSavedListener listener) {
    if (variant.getVariantId() == null || variant.getVariantId().isEmpty()) {
      listener.onFailure(new IllegalArgumentException("Variant ID is required for update"));
      return;
    }

    // Convert variant to Firestore map
    java.util.Map<String, Object> variantMap = variantToMap(variant);

    firestore.collection(COLLECTION_VARIANTS)
        .document(variant.getVariantId())
        .set(variantMap)
        .addOnSuccessListener(aVoid -> {
          Log.d(TAG, "Variant updated successfully: " + variant.getVariantId());
          listener.onSuccess();
        })
        .addOnFailureListener(e -> {
          Log.e(TAG, "Failed to update variant", e);
          listener.onFailure(e);
        });
  }

  /**
   * Delete a variant from Firestore
   * 
   * @param variantId The variant ID to delete
   * @param listener  Callback listener
   */
  public void deleteVariant(String variantId, OnVariantDeletedListener listener) {
    firestore.collection(COLLECTION_VARIANTS)
        .document(variantId)
        .delete()
        .addOnSuccessListener(aVoid -> {
          Log.d(TAG, "Variant deleted successfully: " + variantId);
          listener.onSuccess();
        })
        .addOnFailureListener(e -> {
          Log.e(TAG, "Failed to delete variant", e);
          listener.onFailure(e);
        });
  }

  /**
   * Generate unique variant ID
   * 
   * @param productId The product ID
   * @return Generated variant ID
   */
  private String generateVariantId(String productId) {
    long timestamp = System.currentTimeMillis();
    return productId + "-variant-" + timestamp;
  }

  /**
   * Convert ProductVariant to Firestore map with nested structure
   */
  private java.util.Map<String, Object> variantToMap(ProductVariant variant) {
    java.util.Map<String, Object> map = new java.util.HashMap<>();

    // Main fields
    map.put("productId", variant.getProductId());

    // Attributes group (nested map)
    java.util.Map<String, Object> attributes = new java.util.HashMap<>();
    attributes.put("color", variant.getColor());
    attributes.put("colorHex", variant.getColorHex());
    attributes.put("ram", variant.getRam());
    attributes.put("storage", variant.getStorage());
    map.put("attributes", attributes);

    // Display group (nested map)
    java.util.Map<String, Object> display = new java.util.HashMap<>();
    display.put("name", variant.getName());
    display.put("shortName", variant.getShortName());
    map.put("display", display);

    // Inventory group (nested map)
    java.util.Map<String, Object> inventory = new java.util.HashMap<>();
    inventory.put("isAvailable", variant.isAvailable());
    inventory.put("sku", variant.getSku());
    inventory.put("stockQuantity", variant.getStockQuantity());
    map.put("inventory", inventory);

    return map;
  }
}
