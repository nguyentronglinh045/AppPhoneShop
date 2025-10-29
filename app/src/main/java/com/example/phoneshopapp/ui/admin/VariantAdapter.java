package com.example.phoneshopapp.ui.admin;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phoneshopapp.R;
import com.example.phoneshopapp.models.ProductVariant;
import java.util.List;

/**
 * Adapter for displaying product variants in RecyclerView
 */
public class VariantAdapter extends RecyclerView.Adapter<VariantAdapter.VariantViewHolder> {
  private final List<ProductVariant> variants;
  private final OnEditListener onEditListener;
  private final OnDeleteListener onDeleteListener;

  public interface OnEditListener {
    void onEdit(ProductVariant variant);
  }

  public interface OnDeleteListener {
    void onDelete(ProductVariant variant);
  }

  public VariantAdapter(List<ProductVariant> variants, OnEditListener onEditListener,
      OnDeleteListener onDeleteListener) {
    this.variants = variants;
    this.onEditListener = onEditListener;
    this.onDeleteListener = onDeleteListener;
  }

  @NonNull
  @Override
  public VariantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_variant, parent, false);
    return new VariantViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull VariantViewHolder holder, int position) {
    ProductVariant variant = variants.get(position);

    // Display variant name
    holder.textVariantName.setText(variant.getName() != null ? variant.getName() : variant.getShortName());

    // Display RAM and Storage
    String details = variant.getRam() + " / " + variant.getStorage();
    holder.textVariantDetails.setText(details);

    // Display stock quantity with appropriate color based on availability
    String stockText = "Stock: " + variant.getStockQuantity();
    holder.textVariantStock.setText(stockText);

    // Display availability status
    if (variant.isAvailable()) {
      holder.textVariantAvailable.setText("Available");
      holder.textVariantAvailable.setBackgroundColor(Color.parseColor("#4CAF50")); // holo_green_light

      // Set stock quantity text color based on quantity
      if (variant.getStockQuantity() > 0) {
        holder.textVariantStock.setTextColor(Color.parseColor("#4CAF50")); // holo_green_dark
      } else {
        holder.textVariantStock.setTextColor(Color.parseColor("#FFA500")); // orange
      }
    } else {
      holder.textVariantAvailable.setText("Unavailable");
      holder.textVariantAvailable.setBackgroundColor(Color.parseColor("#F44336")); // holo_red_light
      holder.textVariantStock.setTextColor(Color.GRAY);
    }

    // Set color indicator
    if (variant.getColorHex() != null && !variant.getColorHex().isEmpty()) {
      try {
        holder.colorIndicator.setBackgroundColor(Color.parseColor(variant.getColorHex()));
      } catch (IllegalArgumentException e) {
        holder.colorIndicator.setBackgroundColor(Color.GRAY);
      }
    } else {
      holder.colorIndicator.setBackgroundColor(Color.GRAY);
    }

    // Set click listeners
    holder.btnEdit.setOnClickListener(v -> onEditListener.onEdit(variant));
    holder.btnDelete.setOnClickListener(v -> onDeleteListener.onDelete(variant));
  }

  @Override
  public int getItemCount() {
    return variants.size();
  }

  static class VariantViewHolder extends RecyclerView.ViewHolder {
    View colorIndicator;
    TextView textVariantName, textVariantDetails, textVariantStock, textVariantAvailable;
    ImageButton btnEdit, btnDelete;

    VariantViewHolder(View itemView) {
      super(itemView);
      colorIndicator = itemView.findViewById(R.id.colorIndicator);
      textVariantName = itemView.findViewById(R.id.textVariantName);
      textVariantDetails = itemView.findViewById(R.id.textVariantDetails);
      textVariantStock = itemView.findViewById(R.id.textVariantStock);
      textVariantAvailable = itemView.findViewById(R.id.textVariantAvailable);
      btnEdit = itemView.findViewById(R.id.btnEditVariant);
      btnDelete = itemView.findViewById(R.id.btnDeleteVariant);
    }
  }
}
