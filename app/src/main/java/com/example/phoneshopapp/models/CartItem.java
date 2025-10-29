package com.example.phoneshopapp.models;

import java.util.Date;
import android.util.Log;

public class CartItem {
  private String id; // Firestore document ID
  private String userId; // ID của user sở hữu cart
  private String productId; // ID của sản phẩm (now String)
  private String productName; // Tên sản phẩm
  private String productPrice; // Giá sản phẩm (string format)
  private double productPriceValue; // Giá sản phẩm (number)
  private String productImageUrl; // URL ảnh sản phẩm
  private int productImageResourceId; // Resource ID ảnh sản phẩm
  private String productCategory; // Loại sản phẩm
  private int quantity; // Số lượng
  private Date addedAt; // Thời gian thêm vào cart
  private Date updatedAt; // Thời gian cập nhật

  // Variant information
  private String variantId; // ID của variant đã chọn
  private String variantName; // Display name của variant
  private String variantShortName; // Short name để hiển thị
  private String variantColor; // Màu sắc variant
  private String variantColorHex; // Hex code của màu
  private String variantRam; // RAM của variant
  private String variantStorage; // Storage của variant

  // Constructor mặc định (Firebase yêu cầu)
  public CartItem() {
  }

  // Constructor đầy đủ
  public CartItem(String userId, String productId, String productName, String productPrice,
      double productPriceValue, String productImageUrl, int productImageResourceId,
      String productCategory, int quantity) {
    this.userId = userId;
    this.productId = productId;
    this.productName = productName;
    this.productPrice = productPrice;
    this.productPriceValue = productPriceValue;
    this.productImageUrl = productImageUrl;
    this.productImageResourceId = productImageResourceId;
    this.productCategory = productCategory;
    this.quantity = quantity;
    this.addedAt = new Date();
    this.updatedAt = new Date();
  }

  // Constructor từ Product với variant
  public CartItem(String userId, com.example.phoneshopapp.Product product, int quantity, ProductVariant variant) {
    try {
      Log.d("CartItem", "Creating CartItem from Product: " + product.getName());
      Log.d("CartItem", "Product ID: " + product.getId());
      Log.d("CartItem", "Product Price: " + product.getPrice());
      Log.d("CartItem", "Product PriceValue: " + product.getPriceValue());
      Log.d("CartItem", "Product ImageUrl: " + product.getImageUrl());
      Log.d("CartItem", "Product Category: " + product.getCategory());

      this.userId = userId;
      this.productId = product.getId();
      this.productName = product.getName();
      this.productPrice = product.getPrice();
      this.productPriceValue = product.getPriceValue();
      this.productImageUrl = product.getImageUrl();
      this.productImageResourceId = product.getImageResourceId();
      this.productCategory = product.getCategory();
      this.quantity = quantity;
      this.addedAt = new Date();
      this.updatedAt = new Date();

      // Set variant information if provided
      if (variant != null) {
        this.variantId = variant.getVariantId();
        this.variantName = variant.getName();
        this.variantShortName = variant.getShortName();
        this.variantColor = variant.getColor();
        this.variantColorHex = variant.getColorHex();
        this.variantRam = variant.getRam();
        this.variantStorage = variant.getStorage();
        Log.d("CartItem", "Variant added: " + variant.getShortName());
      }

      Log.d("CartItem", "CartItem created successfully");
    } catch (Exception e) {
      Log.e("CartItem", "Error creating CartItem from Product", e);
      throw e;
    }
  }

  // Backward compatibility: Constructor từ Product without variant
  public CartItem(String userId, com.example.phoneshopapp.Product product, int quantity) {
    this(userId, product, quantity, null);
  }

  // Getters
  public String getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public String getProductId() {
    return productId;
  }

  public String getProductName() {
    return productName;
  }

  public String getProductPrice() {
    return productPrice;
  }

  public double getProductPriceValue() {
    return productPriceValue;
  }

  public String getProductImageUrl() {
    return productImageUrl;
  }

  public int getProductImageResourceId() {
    return productImageResourceId;
  }

  public String getProductCategory() {
    return productCategory;
  }

  public int getQuantity() {
    return quantity;
  }

  public Date getAddedAt() {
    return addedAt;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public String getVariantId() {
    return variantId;
  }

  public String getVariantName() {
    return variantName;
  }

  public String getVariantShortName() {
    return variantShortName;
  }

  public String getVariantColor() {
    return variantColor;
  }

  public String getVariantColorHex() {
    return variantColorHex;
  }

  public String getVariantRam() {
    return variantRam;
  }

  public String getVariantStorage() {
    return variantStorage;
  }

  // Setters
  public void setId(String id) {
    this.id = id;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public void setProductPrice(String productPrice) {
    this.productPrice = productPrice;
  }

  public void setProductPriceValue(double productPriceValue) {
    this.productPriceValue = productPriceValue;
  }

  public void setProductImageUrl(String productImageUrl) {
    this.productImageUrl = productImageUrl;
  }

  public void setProductImageResourceId(int productImageResourceId) {
    this.productImageResourceId = productImageResourceId;
  }

  public void setProductCategory(String productCategory) {
    this.productCategory = productCategory;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
    this.updatedAt = new Date(); // Cập nhật thời gian khi thay đổi quantity
  }

  public void setAddedAt(Date addedAt) {
    this.addedAt = addedAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void setVariantId(String variantId) {
    this.variantId = variantId;
  }

  public void setVariantName(String variantName) {
    this.variantName = variantName;
  }

  public void setVariantShortName(String variantShortName) {
    this.variantShortName = variantShortName;
  }

  public void setVariantColor(String variantColor) {
    this.variantColor = variantColor;
  }

  public void setVariantColorHex(String variantColorHex) {
    this.variantColorHex = variantColorHex;
  }

  public void setVariantRam(String variantRam) {
    this.variantRam = variantRam;
  }

  public void setVariantStorage(String variantStorage) {
    this.variantStorage = variantStorage;
  }

  // Helper methods
  public double getTotalPrice() {
    return productPriceValue * quantity;
  }

  public void increaseQuantity(int amount) {
    this.quantity += amount;
    this.updatedAt = new Date();
  }

  public void decreaseQuantity(int amount) {
    this.quantity = Math.max(1, this.quantity - amount);
    this.updatedAt = new Date();
  }

  // Override equals để so sánh CartItem
  // Two cart items are equal if they have same userId, productId, AND variantId
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;

    CartItem cartItem = (CartItem) obj;

    // Compare userId and productId
    boolean baseEquals = productId != null && productId.equals(cartItem.productId) &&
        userId != null && userId.equals(cartItem.userId);

    if (!baseEquals) {
      return false;
    }

    // Compare variantId - important for differentiating variants of same product
    // If both have variants, they must match
    // If neither has variant, they're equal (backward compatibility)
    if (variantId != null && cartItem.variantId != null) {
      return variantId.equals(cartItem.variantId);
    } else if (variantId == null && cartItem.variantId == null) {
      return true;
    } else {
      // One has variant, one doesn't - they're different
      return false;
    }
  }

  @Override
  public int hashCode() {
    int result = userId != null ? userId.hashCode() : 0;
    result = 31 * result + (productId != null ? productId.hashCode() : 0);
    result = 31 * result + (variantId != null ? variantId.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CartItem{" +
        "id='" + id + '\'' +
        ", userId='" + userId + '\'' +
        ", productId=" + productId +
        ", productName='" + productName + '\'' +
        ", quantity=" + quantity +
        ", totalPrice=" + getTotalPrice() +
        '}';
  }
}