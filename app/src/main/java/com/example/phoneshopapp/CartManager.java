package com.example.phoneshopapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class để quản lý giỏ hàng trong memory
 * Dành cho dự án học tập - không sử dụng database
 */
public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    // Thêm sản phẩm vào giỏ hàng
    public void addToCart(Product product, int quantity) {
        // Kiểm tra xem sản phẩm đã có trong giỏ chưa
        for (CartItem item : cartItems) {
            if (item.getProduct().getId() != null && item.getProduct().getId().equals(product.getId())) {
                // Nếu có rồi thì tăng số lượng
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        // Nếu chưa có thì thêm mới
        cartItems.add(new CartItem(product, quantity));
    }

    // Xóa sản phẩm khỏi giỏ hàng
    public void removeFromCart(String productId) {
        cartItems.removeIf(item -> item.getProduct().getId() != null &&
                item.getProduct().getId().equals(productId));
    }

    // Cập nhật số lượng sản phẩm
    public void updateQuantity(String productId, int newQuantity) {
        if (newQuantity <= 0) {
            removeFromCart(productId);
            return;
        }

        for (CartItem item : cartItems) {
            if (item.getProduct().getId() != null && item.getProduct().getId().equals(productId)) {
                item.setQuantity(newQuantity);
                break;
            }
        }
    }

    // Lấy danh sách sản phẩm trong giỏ
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    // Đếm tổng số lượng sản phẩm trong giỏ
    public int getTotalItemCount() {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getQuantity();
        }
        return total;
    }

    // Tính tổng tiền
    public double getTotalPrice() {
        double total = 0.0;
        for (CartItem item : cartItems) {
            double price = extractPrice(item.getProduct().getPrice());
            total += price * item.getQuantity();
        }
        return total;
    }

    // Kiểm tra giỏ hàng có trống không
    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    // Xóa toàn bộ giỏ hàng
    public void clearCart() {
        cartItems.clear();
    }

    // Kiểm tra sản phẩm có trong giỏ không
    public boolean isInCart(String productId) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId() != null && item.getProduct().getId().equals(productId)) {
                return true;
            }
        }
        return false;
    }

    // Helper method để chuyển đổi giá từ string sang double
    private double extractPrice(String priceString) {
        String numericPrice = priceString.replaceAll("[^0-9.]", "");
        try {
            return Double.parseDouble(numericPrice);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // Inner class để đại diện cho item trong giỏ hàng
    public static class CartItem {
        private Product product;
        private int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getTotalPrice() {
            String priceString = product.getPrice().replaceAll("[^0-9.]", "");
            try {
                double price = Double.parseDouble(priceString);
                return price * quantity;
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
    }
}