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

public class ProductGridAdapter extends RecyclerView.Adapter<ProductGridAdapter.ProductViewHolder> {

  public List<Product> productList;
  private Context context;

  public ProductGridAdapter(List<Product> productList) {
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
      // Load image from Firebase URL using Glide
      Glide.with(context)
          .load(product.getImageUrl())
          .transform(new CenterCrop(), new RoundedCorners(16))
          .placeholder(R.drawable.ic_image_placeholder) // Skeleton placeholder
          .error(R.drawable.ic_image_placeholder) // Error fallback to skeleton
          .into(holder.image);
    } else {
      // Sử dụng ảnh skeleton cho sản phẩm không có URL ảnh
      holder.image.setImageResource(R.drawable.ic_image_placeholder);
    }

    // Click vào sản phẩm để xem chi tiết
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