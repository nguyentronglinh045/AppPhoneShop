package com.example.phoneshopapp.models;

import java.util.Objects;

/**
 * Model class for product variants (color, RAM, storage combinations)
 * Maps to Firebase 'product_variants' collection
 */
public class ProductVariant {
  // Main fields
  private String variantId;
  private String productId;

  // Attributes group
  private String color;
  private String colorHex;
  private String ram;
  private String storage;

  // Display group
  private String name;
  private String shortName;

  // Inventory group
  private boolean isAvailable;
  private String sku;
  private int stockQuantity;

  // Default constructor required for Firebase
  public ProductVariant() {
  }

  // Full constructor
  public ProductVariant(String variantId, String productId, String color, String colorHex,
      String ram, String storage, String name, String shortName,
      boolean isAvailable, String sku, int stockQuantity) {
    this.variantId = variantId;
    this.productId = productId;
    this.color = color;
    this.colorHex = colorHex;
    this.ram = ram;
    this.storage = storage;
    this.name = name;
    this.shortName = shortName;
    this.isAvailable = isAvailable;
    this.sku = sku;
    this.stockQuantity = stockQuantity;
  }

  // Getters
  public String getVariantId() {
    return variantId;
  }

  public String getProductId() {
    return productId;
  }

  public String getColor() {
    return color;
  }

  public String getColorHex() {
    return colorHex;
  }

  public String getRam() {
    return ram;
  }

  public String getStorage() {
    return storage;
  }

  public String getName() {
    return name;
  }

  public String getShortName() {
    return shortName;
  }

  public boolean isAvailable() {
    return isAvailable;
  }

  public String getSku() {
    return sku;
  }

  public int getStockQuantity() {
    return stockQuantity;
  }

  // Setters
  public void setVariantId(String variantId) {
    this.variantId = variantId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public void setColorHex(String colorHex) {
    this.colorHex = colorHex;
  }

  public void setRam(String ram) {
    this.ram = ram;
  }

  public void setStorage(String storage) {
    this.storage = storage;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public void setAvailable(boolean available) {
    isAvailable = available;
  }

  public void setSku(String sku) {
    this.sku = sku;
  }

  public void setStockQuantity(int stockQuantity) {
    this.stockQuantity = stockQuantity;
  }

  // Helper methods
  public boolean isInStock() {
    return isAvailable && stockQuantity > 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ProductVariant that = (ProductVariant) o;
    return Objects.equals(variantId, that.variantId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(variantId);
  }

  @Override
  public String toString() {
    return "ProductVariant{" +
        "variantId='" + variantId + '\'' +
        ", productId='" + productId + '\'' +
        ", color='" + color + '\'' +
        ", ram='" + ram + '\'' +
        ", storage='" + storage + '\'' +
        ", shortName='" + shortName + '\'' +
        ", isAvailable=" + isAvailable +
        ", stockQuantity=" + stockQuantity +
        '}';
  }
}
