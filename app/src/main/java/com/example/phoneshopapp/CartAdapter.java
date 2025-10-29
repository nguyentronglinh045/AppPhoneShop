package com.example.phoneshopapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartManager.CartItem> cartItems;
    private OnCartItemClickListener listener;

    public interface OnCartItemClickListener {
        void onQuantityChanged(String productId, int newQuantity);

        void onRemoveItem(String productId);
    }

    public CartAdapter(List<CartManager.CartItem> cartItems, OnCartItemClickListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartManager.CartItem cartItem = cartItems.get(position);
        Product product = cartItem.getProduct();

        // Set product information
        holder.productName.setText(product.getName());
        holder.productPrice.setText(product.getPrice());

        // Set product image với Glide và skeleton
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            // Load image from URL using Glide
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.ic_image_placeholder) // Skeleton placeholder
                    .error(R.drawable.ic_image_placeholder) // Skeleton fallback
                    .centerCrop()
                    .into(holder.productImage);
        } else {
            // Sử dụng ảnh skeleton cho sản phẩm không có URL ảnh
            holder.productImage.setImageResource(R.drawable.ic_image_placeholder);
        }

        holder.quantity.setText(String.valueOf(cartItem.getQuantity()));
        holder.totalPrice.setText("₫" + String.format("%.0f", cartItem.getTotalPrice()));

        // Set click listeners
        holder.increaseBtn.setOnClickListener(v -> {
            int newQuantity = cartItem.getQuantity() + 1;
            if (listener != null) {
                listener.onQuantityChanged(product.getId(), newQuantity);
            }
        });

        holder.decreaseBtn.setOnClickListener(v -> {
            int newQuantity = cartItem.getQuantity() - 1;
            if (listener != null) {
                listener.onQuantityChanged(product.getId(), newQuantity);
            }
        });

        holder.removeBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveItem(product.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void updateCartItems(List<CartManager.CartItem> newCartItems) {
        this.cartItems = newCartItems;
        notifyDataSetChanged();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView textOriginalPrice;
        TextView quantity;
        TextView totalPrice;
        TextView increaseBtn;
        TextView decreaseBtn;
        ImageView removeBtn;

        CartViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.imageProduct);
            productName = itemView.findViewById(R.id.textProductName);
            productPrice = itemView.findViewById(R.id.textProductPrice);
            textOriginalPrice = itemView.findViewById(R.id.textOriginalPrice);
            quantity = itemView.findViewById(R.id.textQuantity);
            totalPrice = itemView.findViewById(R.id.textTotalPrice);
            increaseBtn = itemView.findViewById(R.id.btnIncrease);
            decreaseBtn = itemView.findViewById(R.id.btnDecrease);
            removeBtn = itemView.findViewById(R.id.btnRemove);
        }
    }
}
