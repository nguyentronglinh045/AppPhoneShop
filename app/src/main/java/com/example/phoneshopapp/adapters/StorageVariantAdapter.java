package com.example.phoneshopapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phoneshopapp.R;
import com.example.phoneshopapp.models.ProductVariant;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter for displaying storage/RAM variants in horizontal RecyclerView
 */
public class StorageVariantAdapter extends RecyclerView.Adapter<StorageVariantAdapter.StorageViewHolder> {

  // Inner class to hold unique storage+RAM combinations
  public static class StorageOption {
    public String storage;
    public String ram;

    public StorageOption(String storage, String ram) {
      this.storage = storage;
      this.ram = ram;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      StorageOption that = (StorageOption) o;
      return storage.equals(that.storage) && ram.equals(that.ram);
    }

    @Override
    public int hashCode() {
      return storage.hashCode() + ram.hashCode();
    }
  }

  private List<StorageOption> storageOptions;
  private int selectedPosition = -1;
  private OnStorageSelectedListener listener;

  public interface OnStorageSelectedListener {
    void onStorageSelected(String storage, String ram, int position);
  }

  public StorageVariantAdapter(OnStorageSelectedListener listener) {
    this.storageOptions = new ArrayList<>();
    this.listener = listener;
  }

  /**
   * Update adapter with unique storage+RAM combinations from variants
   */
  public void setStorageOptions(List<ProductVariant> variants) {
    this.storageOptions.clear();

    // Extract unique storage+RAM combinations
    Set<StorageOption> uniqueOptions = new HashSet<>();
    for (ProductVariant variant : variants) {
      String storage = variant.getStorage();
      String ram = variant.getRam();
      if (storage != null && ram != null) {
        StorageOption option = new StorageOption(storage, ram);
        if (!uniqueOptions.contains(option)) {
          uniqueOptions.add(option);
          this.storageOptions.add(option);
        }
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
  public StorageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.variant_storage_item, parent, false);
    return new StorageViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull StorageViewHolder holder, int position) {
    StorageOption option = storageOptions.get(position);
    boolean isSelected = position == selectedPosition;

    holder.bind(option, isSelected);
  }

  @Override
  public int getItemCount() {
    return storageOptions.size();
  }

  class StorageViewHolder extends RecyclerView.ViewHolder {
    private MaterialCardView cardView;
    private TextView textStorageValue;
    private TextView textRamValue;

    public StorageViewHolder(@NonNull View itemView) {
      super(itemView);
      cardView = (MaterialCardView) itemView;
      textStorageValue = itemView.findViewById(R.id.textStorageValue);
      textRamValue = itemView.findViewById(R.id.textRamValue);

      itemView.setOnClickListener(v -> {
        int position = getAdapterPosition();
        if (position != RecyclerView.NO_POSITION && listener != null) {
          setSelectedPosition(position);
          StorageOption option = storageOptions.get(position);
          listener.onStorageSelected(option.storage, option.ram, position);
        }
      });
    }

    public void bind(StorageOption option, boolean isSelected) {
      textStorageValue.setText(option.storage);
      textRamValue.setText(option.ram + " RAM");

      // Update card appearance based on selection
      if (isSelected) {
        cardView.setStrokeColor(itemView.getContext().getColor(R.color.variant_storage_border_selected));
        cardView.setStrokeWidth(3);
        cardView.setCardBackgroundColor(itemView.getContext().getColor(R.color.variant_storage_bg_selected));
      } else {
        cardView.setStrokeColor(itemView.getContext().getColor(R.color.variant_storage_border_default));
        cardView.setStrokeWidth(2);
        cardView.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.white));
      }
    }
  }
}
