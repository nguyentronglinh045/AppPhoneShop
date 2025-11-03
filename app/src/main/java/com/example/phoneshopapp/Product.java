package com.example.phoneshopapp;

public class Product {
    private String id; // Changed from int to String to match Firebase
    private String name;
    private String price;
    private double priceValue; // Thêm giá trị số để tính toán
    private int imageResourceId; // Giữ lại cho compatibility
    private String imageUrl; // Thêm URL cho Firebase
    private String description;
    private String category;
    private boolean isFeatured;
    private boolean isBestDeal;
    private boolean isFlashSale;
    private String brand; // Thêm thương hiệu
    private int stockQuantity; // Thêm số lượng tồn kho

    // Thông số kỹ thuật
    private String specScreen;
    private String specProcessor;
    private String specRam;
    private String specStorage;

    // Variant flag (hasVariants field from Firebase)
    private boolean hasVariants;

    // Constructor mặc định (Firebase yêu cầu)
    public Product() {
    }

    // Constructor đầy đủ với imageResourceId và brand, stockQuantity
    public Product(String id, String name, String price, int imageResourceId, String description, String category,
            boolean isFeatured, boolean isBestDeal, boolean isFlashSale, String brand, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.priceValue = parsePrice(price);
        this.imageResourceId = imageResourceId;
        this.description = description;
        this.category = category;
        this.isFeatured = isFeatured;
        this.isBestDeal = isBestDeal;
        this.isFlashSale = isFlashSale;
        this.brand = brand;
        this.stockQuantity = stockQuantity;
    }

    // Constructor đầy đủ (backward compatibility)
    public Product(String id, String name, String price, int imageResourceId, String description, String category,
            boolean isFeatured, boolean isBestDeal, boolean isFlashSale) {
        this(id, name, price, imageResourceId, description, category, isFeatured, isBestDeal, isFlashSale, "", 0);
    }

    // Constructor mới với Firebase fields
    public Product(String id, String name, String price, String imageUrl, String description,
            String category, boolean isFeatured, boolean isBestDeal, boolean isFlashSale, String brand, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.priceValue = parsePrice(price);
        this.imageUrl = imageUrl;
        this.description = description;
        this.category = category;
        this.isFeatured = isFeatured;
        this.isBestDeal = isBestDeal;
        this.isFlashSale = isFlashSale;
        this.brand = brand;
        this.stockQuantity = stockQuantity;
    }

    // Constructor ngắn gọn (tương thích với code cũ)
    public Product(String name, String price, int imageResourceId) {
        this("", name, price, imageResourceId, "", "Phone", false, false, false);
    }

    // Helper method để parse price string thành number
    private double parsePrice(String price) {
        if (price == null)
            return 0.0;
        try {
            // Remove currency symbols and parse
            String cleanPrice = price.replaceAll("[^\\d.]", "");
            return Double.parseDouble(cleanPrice);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public double getPriceValue() {
        return priceValue;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public boolean isBestDeal() {
        return isBestDeal;
    }

    public boolean isFlashSale() {
        return isFlashSale;
    }

    public String getBrand() {
        return brand;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public String getSpecScreen() {
        return specScreen;
    }

    public String getSpecProcessor() {
        return specProcessor;
    }

    public String getSpecRam() {
        return specRam;
    }

    public String getSpecStorage() {
        return specStorage;
    }

    public boolean isHasVariants() {
        return hasVariants;
    }

    // Setters để có thể thay đổi từ code
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
        this.priceValue = parsePrice(price);
    }

    public void setPriceValue(double priceValue) {
        this.priceValue = priceValue;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public void setBestDeal(boolean bestDeal) {
        isBestDeal = bestDeal;
    }

    public void setFlashSale(boolean flashSale) {
        isFlashSale = flashSale;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void setSpecScreen(String specScreen) {
        this.specScreen = specScreen;
    }

    public void setSpecProcessor(String specProcessor) {
        this.specProcessor = specProcessor;
    }

    public void setSpecRam(String specRam) {
        this.specRam = specRam;
    }

    public void setSpecStorage(String specStorage) {
        this.specStorage = specStorage;
    }

    public void setHasVariants(boolean hasVariants) {
        this.hasVariants = hasVariants;
    }
}