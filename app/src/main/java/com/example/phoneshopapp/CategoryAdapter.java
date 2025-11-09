package com.example.phoneshopapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

  public List<Category> categoryList;
  private OnCategoryClickListener onCategoryClickListener;

  public interface OnCategoryClickListener {
    void onCategoryClick(Category category);
  }

  public CategoryAdapter(List<Category> categoryList) {
    this.categoryList = categoryList;
  }

  public void setOnCategoryClickListener(OnCategoryClickListener listener) {
    this.onCategoryClickListener = listener;
  }

  @NonNull
  @Override
  public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_category, parent, false);
    return new CategoryViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
    Category category = categoryList.get(position);
    holder.categoryName.setText(category.getName());
    holder.categoryIcon.setImageResource(category.getIconResourceId());
    
    holder.itemView.setOnClickListener(v -> {
      if (onCategoryClickListener != null) {
        onCategoryClickListener.onCategoryClick(category);
      }
    });
  }

  @Override
  public int getItemCount() {
    return categoryList.size();
  }

  static class CategoryViewHolder extends RecyclerView.ViewHolder {
    TextView categoryName;
    ImageView categoryIcon;

    CategoryViewHolder(View itemView) {
      super(itemView);
      categoryName = itemView.findViewById(R.id.textCategoryName);
      categoryIcon = itemView.findViewById(R.id.imageCategoryIcon);
    }
  }
}