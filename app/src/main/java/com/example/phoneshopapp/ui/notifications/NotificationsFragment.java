package com.example.phoneshopapp.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phoneshopapp.CartAdapter;
import com.example.phoneshopapp.CartManager;
import com.example.phoneshopapp.Product;
import com.example.phoneshopapp.ProductManager;
import com.example.phoneshopapp.R;
import com.example.phoneshopapp.databinding.FragmentNotificationsBinding;

import java.util.List;

public class NotificationsFragment extends Fragment implements CartAdapter.OnCartItemClickListener {

    private FragmentNotificationsBinding binding;
    private CartManager cartManager;
    private ProductManager productManager;
    private CartAdapter cartAdapter;
    private RecyclerView recyclerViewCart;
    private LinearLayout layoutEmptyCart;
    private TextView textItemCount;
    private TextView textTotalPrice;
    private Button btnCheckout;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize managers
        cartManager = CartManager.getInstance();
        productManager = ProductManager.getInstance();

        // Initialize views
        recyclerViewCart = binding.recyclerViewCart;
        layoutEmptyCart = binding.layoutEmptyCart;
        textItemCount = binding.textItemCount;
        textTotalPrice = binding.textTotalPrice;
        btnCheckout = binding.btnCheckout;

        // Setup RecyclerView
        setupRecyclerView();

        // Add mock data for testing
        addMockDataToCart();

        // Setup click listeners
        setupClickListeners();

        // Update UI
        updateCartUI();

        return root;
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartManager.getCartItems(), this);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCart.setAdapter(cartAdapter);
    }

    private void addMockDataToCart() {
        // Clear existing cart
        cartManager.clearCart();

        // Load products from Firebase trước khi add vào cart
        productManager.loadProductsFromFirebase(new ProductManager.OnProductsLoadedListener() {
            @Override
            public void onSuccess(List<Product> products) {
                if (products.size() >= 3) {
                    // Add iPhone 15 Pro (quantity 1)
                    cartManager.addToCart(products.get(0), 1);

                    // Add Galaxy S24 Ultra (quantity 2)
                    cartManager.addToCart(products.get(2), 2);

                    // Add OnePlus 12 (quantity 1)
                    cartManager.addToCart(products.get(3), 1);
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Nếu Firebase lỗi thì không add mock data
                Log.e("NotificationsFragment", "Failed to load products for mock cart", e);
            }
        });
    }

    private void setupClickListeners() {
        btnCheckout.setOnClickListener(v -> {
            if (!cartManager.isEmpty()) {
                Toast.makeText(getContext(), "Checkout functionality coming soon!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCartUI() {
        List<CartManager.CartItem> cartItems = cartManager.getCartItems();

        if (cartManager.isEmpty()) {
            // Show empty cart message
            recyclerViewCart.setVisibility(View.GONE);
            layoutEmptyCart.setVisibility(View.VISIBLE);
            textItemCount.setText("0 items");
            textTotalPrice.setText("$0.00");
            btnCheckout.setEnabled(false);
        } else {
            // Show cart items
            recyclerViewCart.setVisibility(View.VISIBLE);
            layoutEmptyCart.setVisibility(View.GONE);

            // Update item count
            int totalItems = cartManager.getTotalItemCount();
            textItemCount.setText(totalItems + " item" + (totalItems > 1 ? "s" : ""));

            // Update total price
            double totalPrice = cartManager.getTotalPrice();
            textTotalPrice.setText("$" + String.format("%.2f", totalPrice));

            // Enable checkout button
            btnCheckout.setEnabled(true);

            // Update adapter
            cartAdapter.updateCartItems(cartItems);
        }
    }

    @Override
    public void onQuantityChanged(String productId, int newQuantity) {
        cartManager.updateQuantity(productId, newQuantity);
        updateCartUI();
    }

    @Override
    public void onRemoveItem(String productId) {
        cartManager.removeFromCart(productId);
        updateCartUI();
        Toast.makeText(getContext(), "Item removed from cart", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}