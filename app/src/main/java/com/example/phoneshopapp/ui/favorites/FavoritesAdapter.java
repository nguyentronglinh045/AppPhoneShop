package com.example.phoneshopapp.ui.favorites;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.phoneshopapp.R;
import com.example.phoneshopapp.data.favorite.FavoriteManager;
import com.example.phoneshopapp.models.FavoriteItem;

import java.util.List;

/**
 * Adapter for displaying favorite items in RecyclerView
 * Supports click to view details and remove from favorites
 */
public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    private List<FavoriteItem> items;
    private Context context;
    private OnFavoriteItemClickListener listener;

    public interface OnFavoriteItemClickListener {
        void onItemClick(FavoriteItem item);
        void onRemoveClick(FavoriteItem item);
    }

    public FavoritesAdapter(Context context, List<FavoriteItem> items, OnFavoriteItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
            .inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteItem item = items.get(position);

        holder.textName.setText(item.getProductName());
        holder.textPrice.setText(item.getProductPrice());
        
        // Set category if available
        if (item.getProductCategory() != null && !item.getProductCategory().isEmpty()) {
            holder.textCategory.setText(item.getProductCategory());
            holder.textCategory.setVisibility(View.VISIBLE);
        } else {
            holder.textCategory.setVisibility(View.GONE);
        }

        // Load product image
        if (item.getProductImageUrl() != null && !item.getProductImageUrl().isEmpty()) {
            Glide.with(context)
                .load(item.getProductImageUrl())
                .transform(new CenterCrop(), new RoundedCorners(12))
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(holder.imageProduct);
        } else {
            holder.imageProduct.setImageResource(R.drawable.ic_image_placeholder);
        }

        // Click to view product details
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

        // Click remove button
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Update adapter data
     */
    public void updateData(List<FavoriteItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    /**
     * Remove item at position (for swipe-to-delete)
     */
    public void removeItem(int position) {
        if (position < 0 || position >= items.size()) {
            return;
        }

        FavoriteItem item = items.get(position);
        items.remove(position);
        notifyItemRemoved(position);

        // Remove from Firestore
        FavoriteManager.getInstance().removeFavorite(item.getId(), 
            new FavoriteManager.OnFavoriteOperationListener() {
                @Override
                public void onSuccess(String message) {
                    // Item already removed from UI
                }

                @Override
                public void onFailure(String error) {
                    // Re-add to list if failed
                    items.add(position, item);
                    notifyItemInserted(position);
                }
            });
    }

    /**
     * Get item at position (for swipe callback)
     */
    public FavoriteItem getItemAt(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textName;
        TextView textPrice;
        TextView textCategory;
        ImageView btnRemove;

        ViewHolder(View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            textName = itemView.findViewById(R.id.textProductName);
            textPrice = itemView.findViewById(R.id.textProductPrice);
            textCategory = itemView.findViewById(R.id.textProductCategory);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
