package com.example.phoneshopapp;

import android.content.Context;
import android.content.Intent;
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

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public List<Product> productList;
    private Context context;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product_grid, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.name.setText(product.getName());
        holder.price.setText(product.getPrice());

        // Load image from URL if available, otherwise use resource ID
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrl())
                    .transform(new CenterCrop(), new RoundedCorners(16))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.ic_image_placeholder);
        }

        // Bind rating stars, rating text, review count nếu layout có
        try {
            float rating = 4.0f;
            int reviewCount = 0;
            if (product instanceof Product) {
                // Nếu Product có trường rating và reviewCount, lấy ra
                // (bạn có thể sửa lại cho đúng model nếu cần)
                // rating = product.getRating();
                // reviewCount = product.getReviewCount();
            }
            ImageView[] stars = new ImageView[] {
                holder.itemView.findViewById(R.id.star1),
                holder.itemView.findViewById(R.id.star2),
                holder.itemView.findViewById(R.id.star3),
                holder.itemView.findViewById(R.id.star4),
                holder.itemView.findViewById(R.id.star5)
            };
            for (int i = 0; i < 5; i++) {
                if (i < Math.round(rating)) {
                    stars[i].setImageResource(R.drawable.ic_star_filled);
                } else {
                    stars[i].setImageResource(R.drawable.ic_star_empty);
                }
            }
            TextView textRating = holder.itemView.findViewById(R.id.textRating);
            TextView textReviewCount = holder.itemView.findViewById(R.id.textReviewCount);
            if (textRating != null) textRating.setText(String.valueOf(rating));
            if (textReviewCount != null) textReviewCount.setText("(" + reviewCount + ")");
        } catch (Exception ignore) {}

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // Method để update data từ Firebase
    public void updateData(List<Product> newProductList) {
        this.productList = newProductList;
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name, price;
        ImageView image;

        ProductViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textProductName);
            price = itemView.findViewById(R.id.textProductPrice);
            image = itemView.findViewById(R.id.imageProduct);
        }
    }
}