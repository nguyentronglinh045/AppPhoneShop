package com.example.phoneshopapp.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phoneshopapp.R;
import com.example.phoneshopapp.models.ProductVariant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter for displaying color variants in horizontal RecyclerView
 */
public class ColorVariantAdapter extends RecyclerView.Adapter<ColorVariantAdapter.ColorViewHolder> {

  private List<String> colors; // Unique colors
  private List<String> colorHexes; // Corresponding hex values
  private int selectedPosition = -1;
  private OnColorSelectedListener listener;

  public interface OnColorSelectedListener {
    void onColorSelected(String color, int position);
  }

  public ColorVariantAdapter(OnColorSelectedListener listener) {
    this.colors = new ArrayList<>();
    this.colorHexes = new ArrayList<>();
    this.listener = listener;
  }

  /**
   * Update adapter with unique colors from variants
   */
  public void setColors(List<ProductVariant> variants) {
    this.colors.clear();
    this.colorHexes.clear();

    // Extract unique colors
    Set<String> uniqueColors = new HashSet<>();
    for (ProductVariant variant : variants) {
      String color = variant.getColor();
      if (color != null && !uniqueColors.contains(color)) {
        uniqueColors.add(color);
        this.colors.add(color);
        this.colorHexes.add(variant.getColorHex());
      }
    }

    notifyDataSetChanged();
  }

  public void setSelectedPosition(int position) {
    int oldPosition = selectedPosition;
    selectedPosition = position;

    if (oldPosition >= 0) {
      notifyItemChanged(oldPosition);
    }
    if (selectedPosition >= 0) {
      notifyItemChanged(selectedPosition);
    }
  }

  @NonNull
  @Override
  public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.variant_color_item, parent, false);
    return new ColorViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
    String color = colors.get(position);
    String colorHex = colorHexes.get(position);
    boolean isSelected = position == selectedPosition;

    holder.bind(color, colorHex, isSelected);
  }

  @Override
  public int getItemCount() {
    return colors.size();
  }

  class ColorViewHolder extends RecyclerView.ViewHolder {
    private View colorPreview;
    private View colorBorder;
    private TextView textColorName;

    public ColorViewHolder(@NonNull View itemView) {
      super(itemView);
      colorPreview = itemView.findViewById(R.id.colorPreview);
      colorBorder = itemView.findViewById(R.id.colorBorder);
      textColorName = itemView.findViewById(R.id.textColorName);

      itemView.setOnClickListener(v -> {
        int position = getAdapterPosition();
        if (position != RecyclerView.NO_POSITION && listener != null) {
          setSelectedPosition(position);
          listener.onColorSelected(colors.get(position), position);
        }
      });
    }

    public void bind(String color, String colorHex, boolean isSelected) {
      textColorName.setText(color);

      // Set color preview
      GradientDrawable colorDrawable = (GradientDrawable) colorPreview.getBackground();
      try {
        colorDrawable.setColor(Color.parseColor(colorHex));
      } catch (IllegalArgumentException e) {
        colorDrawable.setColor(Color.BLACK);
      }

      // Set border based on selection
      if (isSelected) {
        colorBorder.setBackgroundResource(R.drawable.variant_color_border_selected);
      } else {
        colorBorder.setBackgroundResource(R.drawable.variant_color_border_default);
      }
    }
  }
}
