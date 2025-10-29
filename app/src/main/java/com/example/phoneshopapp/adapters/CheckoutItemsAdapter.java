package com.example.phoneshopapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.phoneshopapp.R;
import com.example.phoneshopapp.models.OrderItem;
import com.google.android.material.textview.MaterialTextView;
import java.util.ArrayList;
import java.util.List;

public class CheckoutItemsAdapter extends RecyclerView.Adapter<CheckoutItemsAdapter.ViewHolder> {

    private List<OrderItem> items = new ArrayList<>();

    public void updateItems(List<OrderItem> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkout_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageProduct;
        private MaterialTextView textProductName;
        private MaterialTextView textPrice;
        private MaterialTextView textQuantity;
        private MaterialTextView textTotalPrice;

        // Variant views
        private View layoutVariantInfo;
        private View viewVariantColorIndicator;
        private MaterialTextView textVariantInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            textProductName = itemView.findViewById(R.id.textProductName);
            textPrice = itemView.findViewById(R.id.textPrice);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textTotalPrice = itemView.findViewById(R.id.textTotalPrice);

            // Initialize variant views
            layoutVariantInfo = itemView.findViewById(R.id.layoutVariantInfo);
            viewVariantColorIndicator = itemView.findViewById(R.id.viewVariantColorIndicator);
            textVariantInfo = itemView.findViewById(R.id.textVariantInfo);
        }

        public void bind(OrderItem item) {
            textProductName.setText(item.getProductName());
            textPrice.setText(item.getFormattedPrice());
            textQuantity.setText(String.format("x%d", item.getQuantity()));
            textTotalPrice.setText(item.getFormattedTotalPrice());

            // Display variant info if available
            if (item.getVariantId() != null && item.getVariantShortName() != null) {
                layoutVariantInfo.setVisibility(View.VISIBLE);
                textVariantInfo.setText(item.getVariantShortName());

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
                // Hide variant info for products without variants
                layoutVariantInfo.setVisibility(View.GONE);
            }

            // Load product image
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getImageUrl())
                        .placeholder(R.drawable.ic_phone_placeholder)
                        .error(R.drawable.ic_phone_placeholder)
                        .into(imageProduct);
            } else {
                imageProduct.setImageResource(R.drawable.ic_phone_placeholder);
            }
        }
    }
}