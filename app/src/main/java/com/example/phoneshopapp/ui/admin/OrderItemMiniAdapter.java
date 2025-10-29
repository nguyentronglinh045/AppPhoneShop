package com.example.phoneshopapp.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.phoneshopapp.R;
import com.example.phoneshopapp.models.OrderItem;
import java.util.List;

/**
 * Mini adapter for showing order items in compact form within order list
 */
public class OrderItemMiniAdapter extends RecyclerView.Adapter<OrderItemMiniAdapter.OrderItemViewHolder> {

  private final List<OrderItem> orderItems;
  private final int maxItems;

  /**
   * Constructor for the adapter
   * 
   * @param orderItems List of order items to display
   * @param maxItems   Maximum number of items to show (rest will be counted)
   */
  public OrderItemMiniAdapter(List<OrderItem> orderItems, int maxItems) {
    this.orderItems = orderItems;
    this.maxItems = maxItems;
  }

  @NonNull
  @Override
  public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_order_product_mini, parent, false);
    return new OrderItemViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
    if (position < orderItems.size()) {
      OrderItem item = orderItems.get(position);

      // Show product name
      holder.textProductName.setText(item.getProductName());

      // Show variant info if available
      if (item.getVariantShortName() != null && !item.getVariantShortName().isEmpty()) {
        holder.textVariantName.setText(item.getVariantShortName());
        holder.textVariantName.setVisibility(View.VISIBLE);
      } else {
        holder.textVariantName.setVisibility(View.GONE);
      }

      // Show quantity
      holder.textQuantity.setText("x" + item.getQuantity());

      // Load image using Glide
      if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
        try {
          Glide.with(holder.imageProduct.getContext())
              .load(item.getImageUrl())
              .placeholder(R.drawable.ic_image_placeholder)
              .error(R.drawable.ic_image_placeholder)
              .centerCrop()
              .into(holder.imageProduct);
        } catch (Exception e) {
          // Fallback if loading fails
          holder.imageProduct.setImageResource(R.drawable.ic_image_placeholder);
        }
      } else {
        holder.imageProduct.setImageResource(R.drawable.ic_image_placeholder);
      }
    } else if (position == maxItems - 1 && orderItems.size() > maxItems) {
      // This is the last visible item and there are more items
      int moreItems = orderItems.size() - maxItems + 1;
      holder.textProductName.setText("+" + moreItems + " more item" + (moreItems > 1 ? "s" : ""));
      holder.textVariantName.setVisibility(View.GONE);
      holder.textQuantity.setVisibility(View.GONE);
      holder.imageProduct.setImageResource(R.drawable.ic_more);
    }
  }

  @Override
  public int getItemCount() {
    // If there are more items than maxItems, show maxItems items
    // If not, show all items
    return Math.min(orderItems.size(), maxItems);
  }

  /**
   * ViewHolder for order item view
   */
  static class OrderItemViewHolder extends RecyclerView.ViewHolder {

    ImageView imageProduct;
    TextView textProductName;
    TextView textVariantName;
    TextView textQuantity;

    OrderItemViewHolder(@NonNull View itemView) {
      super(itemView);
      imageProduct = itemView.findViewById(R.id.imageProduct);
      textProductName = itemView.findViewById(R.id.textProductName);
      textVariantName = itemView.findViewById(R.id.textVariantName);
      textQuantity = itemView.findViewById(R.id.textQuantity);
    }
  }
}
