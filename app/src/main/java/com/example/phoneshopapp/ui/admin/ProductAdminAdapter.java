package com.example.phoneshopapp.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phoneshopapp.Product;
import com.example.phoneshopapp.R;
import java.util.List;

public class ProductAdminAdapter extends RecyclerView.Adapter<ProductAdminAdapter.ProductViewHolder> {
  private final List<Product> products;
  private final OnEditListener onEditListener;
  private final OnDeleteListener onDeleteListener;
  private final OnManageVariantsListener onManageVariantsListener;

  public interface OnEditListener {
    void onEdit(Product product);
  }

  public interface OnDeleteListener {
    void onDelete(Product product);
  }

  public interface OnManageVariantsListener {
    void onManageVariants(Product product);
  }

  public ProductAdminAdapter(List<Product> products, OnEditListener onEditListener,
      OnDeleteListener onDeleteListener, OnManageVariantsListener onManageVariantsListener) {
    this.products = products;
    this.onEditListener = onEditListener;
    this.onDeleteListener = onDeleteListener;
    this.onManageVariantsListener = onManageVariantsListener;
  }

  @NonNull
  @Override
  public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_product, parent, false);
    return new ProductViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
    Product product = products.get(position);
    holder.textName.setText(product.getName());
    holder.textPrice.setText("Giá: " + product.getPrice());
    // Hiển thị ảnh sản phẩm
    android.widget.ImageView imageView = holder.imageProduct;
    if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
      // Sử dụng Glide để load ảnh từ URL
      com.bumptech.glide.Glide.with(imageView.getContext())
          .load(product.getImageUrl())
          .placeholder(R.drawable.ic_image_placeholder)
          .error(R.drawable.ic_image_placeholder)
          .into(imageView);
    } else {
      // Hiển thị ảnh skeleton cho sản phẩm không có URL ảnh
      imageView.setImageResource(R.drawable.ic_image_placeholder);
    }
    holder.btnManageVariants.setOnClickListener(v -> onManageVariantsListener.onManageVariants(product));
    holder.btnEdit.setOnClickListener(v -> onEditListener.onEdit(product));
    holder.btnDelete.setOnClickListener(v -> onDeleteListener.onDelete(product));
    // Show all buttons
    holder.btnManageVariants.setVisibility(View.VISIBLE);
    holder.btnEdit.setVisibility(View.VISIBLE);
    holder.btnDelete.setVisibility(View.VISIBLE);
  }

  @Override
  public int getItemCount() {
    return products.size();
  }

  static class ProductViewHolder extends RecyclerView.ViewHolder {
    TextView textName, textPrice;
    ImageButton btnManageVariants, btnEdit, btnDelete;
    android.widget.ImageView imageProduct;

    ProductViewHolder(View itemView) {
      super(itemView);
      textName = itemView.findViewById(R.id.textProductName);
      textPrice = itemView.findViewById(R.id.textProductPrice);
      btnManageVariants = itemView.findViewById(R.id.btnManageVariants);
      btnEdit = itemView.findViewById(R.id.btnEditProduct);
      btnDelete = itemView.findViewById(R.id.btnDeleteProduct);
      imageProduct = itemView.findViewById(R.id.imageProduct);
    }
  }
}
