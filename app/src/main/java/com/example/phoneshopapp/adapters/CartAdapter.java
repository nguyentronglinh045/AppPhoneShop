package com.example.phoneshopapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.phoneshopapp.R;
import com.example.phoneshopapp.models.CartItem;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
  private List<CartItem> cartItems;
  private OnCartItemActionListener listener;

  public interface OnCartItemActionListener {
    void onQuantityChanged(CartItem item, int newQuantity);

    void onItemRemoved(CartItem item);

    void onItemSelectionChanged(CartItem item, boolean isSelected);
  }

  public CartAdapter(OnCartItemActionListener listener) {
    this.cartItems = new ArrayList<>();
    this.listener = listener;
  }

  @NonNull
  @Override
  public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.cart_item, parent, false);
    return new CartViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
    CartItem item = cartItems.get(position);
    holder.bind(item);
  }

  @Override
  public int getItemCount() {
    return cartItems.size();
  }

  public void updateCartItems(List<CartItem> newItems) {
    this.cartItems.clear();
    this.cartItems.addAll(newItems);
    notifyDataSetChanged();
  }

  public void removeItem(int position) {
    if (position >= 0 && position < cartItems.size()) {
      cartItems.remove(position);
      notifyItemRemoved(position);
    }
  }

  class CartViewHolder extends RecyclerView.ViewHolder {
    private com.google.android.material.checkbox.MaterialCheckBox checkboxSelectItem;
    private ImageView imageProduct;
    private TextView textProductName, textProductCategory, textProductPrice;
    private TextView textQuantity, textTotalPrice;
    private MaterialButton btnMinus, btnPlus, btnRemove;

    // Variant views
    private android.widget.LinearLayout layoutVariantInfo;
    private android.view.View viewVariantColorIndicator;
    private TextView textCartVariantInfo;

    public CartViewHolder(@NonNull View itemView) {
      super(itemView);

      checkboxSelectItem = itemView.findViewById(R.id.checkboxSelectItem);
      imageProduct = itemView.findViewById(R.id.imageCartProduct);
      textProductName = itemView.findViewById(R.id.textCartProductName);
      textProductCategory = itemView.findViewById(R.id.textCartProductCategory);
      textProductPrice = itemView.findViewById(R.id.textCartProductPrice);
      textQuantity = itemView.findViewById(R.id.textCartQuantity);
      textTotalPrice = itemView.findViewById(R.id.textCartTotalPrice);
      btnMinus = itemView.findViewById(R.id.btnCartMinus);
      btnPlus = itemView.findViewById(R.id.btnCartPlus);
      btnRemove = itemView.findViewById(R.id.btnCartRemove);

      // Initialize variant views
      layoutVariantInfo = itemView.findViewById(R.id.layoutVariantInfo);
      viewVariantColorIndicator = itemView.findViewById(R.id.viewVariantColorIndicator);
      textCartVariantInfo = itemView.findViewById(R.id.textCartVariantInfo);
    }

    public void bind(CartItem item) {
      // Bind checkbox selection state
      checkboxSelectItem.setOnCheckedChangeListener(null); // Remove old listener
      checkboxSelectItem.setChecked(item.isSelected());
      
      // Handle checkbox selection change
      checkboxSelectItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
        if (buttonView.isPressed()) { // Only handle user interaction, not programmatic changes
          item.setSelected(isChecked);
          if (listener != null) {
            listener.onItemSelectionChanged(item, isChecked);
          }
        }
      });

      // Set product info
      textProductName.setText(item.getProductName());
      textProductCategory.setText(item.getProductCategory());
      textProductPrice.setText(item.getProductPrice());
      textQuantity.setText(String.valueOf(item.getQuantity()));

      // Calculate and display total price
      double totalPrice = item.getTotalPrice();
      textTotalPrice.setText(String.format("Tổng: ₫%.0f", totalPrice));

      // Display variant info if available
      if (item.getVariantId() != null && item.getVariantShortName() != null) {
        layoutVariantInfo.setVisibility(android.view.View.VISIBLE);
        textCartVariantInfo.setText(item.getVariantShortName());

        // Set color indicator if color hex is available
        if (item.getVariantColorHex() != null) {
          try {
            android.graphics.drawable.GradientDrawable colorDrawable = (android.graphics.drawable.GradientDrawable) viewVariantColorIndicator
                .getBackground();
            colorDrawable.setColor(android.graphics.Color.parseColor(item.getVariantColorHex()));
          } catch (Exception e) {
            // If parsing fails, use default black
            android.graphics.drawable.GradientDrawable colorDrawable = (android.graphics.drawable.GradientDrawable) viewVariantColorIndicator
                .getBackground();
            colorDrawable.setColor(android.graphics.Color.BLACK);
          }
        }
      } else {
        // Hide variant info for products without variants (backward compatibility)
        layoutVariantInfo.setVisibility(android.view.View.GONE);
      }

      // Set product image with Glide
      if (item.getProductImageUrl() != null && !item.getProductImageUrl().isEmpty()) {
        // Load image from URL using Glide
        Glide.with(itemView.getContext())
            .load(item.getProductImageUrl())
            .placeholder(R.drawable.ic_image_placeholder) // Skeleton placeholder while loading
            .error(R.drawable.ic_image_placeholder) // Skeleton fallback if load fails
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(imageProduct);
      } else if (item.getProductImageResourceId() != 0) {
        // Load from resource ID
        Glide.with(itemView.getContext())
            .load(item.getProductImageResourceId())
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder)
            .centerCrop()
            .into(imageProduct);
      } else {
        // Sử dụng ảnh skeleton cho sản phẩm không có ảnh
        imageProduct.setImageResource(R.drawable.ic_image_placeholder);
      }

      // Set click listeners
      btnMinus.setOnClickListener(v -> {
        int currentQuantity = item.getQuantity();
        if (currentQuantity > 1) {
          int newQuantity = currentQuantity - 1;
          item.setQuantity(newQuantity); // Update local object
          textQuantity.setText(String.valueOf(newQuantity));
          textTotalPrice.setText(String.format("Tổng: ₫%.0f", item.getTotalPrice()));

          if (listener != null) {
            listener.onQuantityChanged(item, newQuantity);
          }
        }
      });

      btnPlus.setOnClickListener(v -> {
        int currentQuantity = item.getQuantity();
        if (currentQuantity < 99) {
          int newQuantity = currentQuantity + 1;
          item.setQuantity(newQuantity); // Update local object
          textQuantity.setText(String.valueOf(newQuantity));
          textTotalPrice.setText(String.format("Tổng: ₫%.0f", item.getTotalPrice()));

          if (listener != null) {
            listener.onQuantityChanged(item, newQuantity);
          }
        }
      });

      btnRemove.setOnClickListener(v -> {
        if (listener != null) {
          listener.onItemRemoved(item);
        }
      });
    }
  }
}