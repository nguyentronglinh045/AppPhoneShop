package com.example.phoneshopapp.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phoneshopapp.ManageVariantsActivity;
import com.example.phoneshopapp.Product;
import com.example.phoneshopapp.ProductManager;
import com.example.phoneshopapp.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for managing products in admin panel
 */
public class ProductsTabFragment extends Fragment {
  private RecyclerView recyclerProducts;
  private Button btnAddProduct;
  private ProductAdminAdapter adapter;
  private List<Product> productList = new ArrayList<>();

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_products_tab, container, false);

    recyclerProducts = root.findViewById(R.id.recyclerProducts);
    btnAddProduct = root.findViewById(R.id.btnAddProduct);

    adapter = new ProductAdminAdapter(productList, this::onEditProduct, this::onDeleteProduct, this::onManageVariants);
    recyclerProducts.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerProducts.setAdapter(adapter);

    btnAddProduct.setOnClickListener(v -> onAddProduct());

    loadProducts();
    return root;
  }

  private void loadProducts() {
    ProductManager.getInstance()
        .loadProductsFromFirebase(new ProductManager.OnProductsLoadedListener() {
          @Override
          public void onSuccess(List<Product> products) {
            productList.clear();
            productList.addAll(products);
            adapter.notifyDataSetChanged();
          }

          @Override
          public void onFailure(Exception e) {
            Toast.makeText(getContext(), "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
          }
        });
  }

  private void forceRefreshProducts() {
    ProductManager.getInstance()
        .forceRefreshFromFirebase(new ProductManager.OnProductsLoadedListener() {
          @Override
          public void onSuccess(List<Product> products) {
            productList.clear();
            productList.addAll(products);
            adapter.notifyDataSetChanged();
          }

          @Override
          public void onFailure(Exception e) {
            Toast.makeText(getContext(), "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
          }
        });
  }

  private void onAddProduct() {
    // Create dialog for adding new product
    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
    builder.setView(dialogView);

    // Get views from dialog
    com.google.android.material.textfield.TextInputEditText editName = dialogView.findViewById(R.id.editTextName);
    com.google.android.material.textfield.TextInputEditText editPrice = dialogView.findViewById(R.id.editTextPrice);
    com.google.android.material.textfield.TextInputEditText editBrand = dialogView.findViewById(R.id.editTextBrand);
    com.google.android.material.textfield.TextInputEditText editCategory = dialogView
        .findViewById(R.id.editTextCategory);
    com.google.android.material.textfield.TextInputEditText editDescription = dialogView
        .findViewById(R.id.editTextDescription);
    com.google.android.material.textfield.TextInputEditText editImageUrl = dialogView
        .findViewById(R.id.editTextImageUrl);
    android.widget.CheckBox checkFeatured = dialogView.findViewById(R.id.checkBoxFeatured);
    android.widget.CheckBox checkBestDeal = dialogView.findViewById(R.id.checkBoxDeal);
    android.widget.Button btnCancel = dialogView.findViewById(R.id.btnCancel);
    android.widget.Button btnSave = dialogView.findViewById(R.id.btnSave);

    android.app.AlertDialog dialog = builder.create();

    // Handle Cancel button
    btnCancel.setOnClickListener(v -> dialog.dismiss());

    // Handle Save button
    btnSave.setOnClickListener(v -> {
      String name = editName.getText().toString().trim();
      String price = editPrice.getText().toString().trim();
      String brand = editBrand.getText().toString().trim();
      String category = editCategory.getText().toString().trim();
      String description = editDescription.getText().toString().trim();
      String imageUrl = editImageUrl.getText().toString().trim();

      if (name.isEmpty() || price.isEmpty()) {
        Toast.makeText(getContext(), "Vui lòng nhập tên và giá sản phẩm", Toast.LENGTH_SHORT).show();
        return;
      }

      // Generate ID and save product with hasVariants = true
      generateProductIdAndSave(name, price, brand, category, description, imageUrl,
          checkFeatured.isChecked(), checkBestDeal.isChecked(), dialog);
    });

    dialog.show();
  }

  private void onEditProduct(Product product) {
    // Create dialog for editing product
    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_product, null);
    builder.setView(dialogView);

    // Get views from dialog
    com.google.android.material.textfield.TextInputEditText editName = dialogView.findViewById(R.id.editProductName);
    com.google.android.material.textfield.TextInputEditText editPrice = dialogView.findViewById(R.id.editProductPrice);
    com.google.android.material.textfield.TextInputEditText editBrand = dialogView.findViewById(R.id.editProductBrand);
    com.google.android.material.textfield.TextInputEditText editCategory = dialogView
        .findViewById(R.id.editProductCategory);
    com.google.android.material.textfield.TextInputEditText editDescription = dialogView
        .findViewById(R.id.editProductDescription);
    com.google.android.material.textfield.TextInputEditText editImageUrl = dialogView
        .findViewById(R.id.editProductImageUrl);
    android.widget.CheckBox checkFeatured = dialogView.findViewById(R.id.checkFeatured);
    android.widget.CheckBox checkBestDeal = dialogView.findViewById(R.id.checkBestDeal);
    android.widget.Button btnCancel = dialogView.findViewById(R.id.btnCancel);
    android.widget.Button btnSave = dialogView.findViewById(R.id.btnSave);

    // Pre-fill current information
    editName.setText(product.getName());
    editPrice.setText(product.getPrice());
    editBrand.setText(product.getBrand());
    editCategory.setText(product.getCategory());
    editDescription.setText(product.getDescription());
    editImageUrl.setText(product.getImageUrl() != null ? product.getImageUrl() : "");
    checkFeatured.setChecked(product.isFeatured());
    checkBestDeal.setChecked(product.isBestDeal());

    android.app.AlertDialog dialog = builder.create();

    // Handle Cancel button
    btnCancel.setOnClickListener(v -> dialog.dismiss());

    // Handle Save button
    btnSave.setOnClickListener(v -> {
      String name = editName.getText().toString().trim();
      String price = editPrice.getText().toString().trim();
      String brand = editBrand.getText().toString().trim();
      String category = editCategory.getText().toString().trim();
      String description = editDescription.getText().toString().trim();
      String imageUrl = editImageUrl.getText().toString().trim();

      if (name.isEmpty() || price.isEmpty()) {
        Toast.makeText(getContext(), "Vui lòng nhập tên và giá sản phẩm", Toast.LENGTH_SHORT).show();
        return;
      }

      // Update product information
      product.setName(name);
      product.setPrice(price);
      product.setBrand(brand);
      product.setCategory(category);
      product.setDescription(description);
      product.setImageUrl(imageUrl);
      product.setFeatured(checkFeatured.isChecked());
      product.setBestDeal(checkBestDeal.isChecked());
      // Always set hasVariants to true
      product.setHasVariants(true);

      // Update to Firestore
      FirebaseFirestore db = FirebaseFirestore.getInstance();
      db.collection("PhoneDB")
          .document(product.getId())
          .set(product)
          .addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Đã cập nhật sản phẩm: " + product.getName(), Toast.LENGTH_SHORT).show();
            forceRefreshProducts();
            dialog.dismiss();
          })
          .addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Lỗi cập nhật sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
          });
    });

    dialog.show();
  }

  private void onDeleteProduct(Product product) {
    // Confirmation dialog for deleting product
    new androidx.appcompat.app.AlertDialog.Builder(getContext())
        .setTitle("Xóa sản phẩm")
        .setMessage("Bạn có chắc chắn muốn xóa \"" + product.getName() + "\" không?")
        .setPositiveButton("Xóa", (dialog, which) -> {
          // Delete product from Firestore
          FirebaseFirestore db = FirebaseFirestore.getInstance();
          db.collection("PhoneDB")
              .document(product.getId())
              .delete()
              .addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Đã xóa sản phẩm: " + product.getName(), Toast.LENGTH_SHORT).show();
                forceRefreshProducts();
              })
              .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Lỗi xóa sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
              });
        })
        .setNegativeButton("Hủy", null)
        .show();
  }

  private void onManageVariants(Product product) {
    // Open ManageVariantsActivity
    Intent intent = new Intent(getContext(), ManageVariantsActivity.class);
    intent.putExtra("productId", product.getId());
    intent.putExtra("productName", product.getName());
    startActivity(intent);
  }

  private void generateProductIdAndSave(String name, String price, String brand, String category,
      String description, String imageUrl, boolean isFeatured,
      boolean isBestDeal, android.app.AlertDialog dialog) {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Generate unique document ID using timestamp
    long timestamp = System.currentTimeMillis();
    String newId = "product-" + timestamp;

    // Create new product with generated ID
    Product newProduct = new Product();
    newProduct.setId(newId);
    newProduct.setName(name);
    newProduct.setPrice(price);
    newProduct.setBrand(brand);
    newProduct.setCategory(category);
    newProduct.setDescription(description);
    newProduct.setImageUrl(imageUrl.isEmpty() ? null : imageUrl);
    newProduct.setFeatured(isFeatured);
    newProduct.setBestDeal(isBestDeal);
    newProduct.setStockQuantity(0); // Stock managed by variants
    newProduct.setHasVariants(true); // Always true for new products

    // Save product to Firestore
    db.collection("PhoneDB")
        .document(newId)
        .set(newProduct)
        .addOnSuccessListener(aVoid -> {
          Toast.makeText(getContext(), "Đã thêm sản phẩm. Vui lòng thêm biến thể ngay.", Toast.LENGTH_LONG).show();
          forceRefreshProducts();
          dialog.dismiss();

          // Auto-open ManageVariantsActivity
          Intent intent = new Intent(getContext(), ManageVariantsActivity.class);
          intent.putExtra("productId", newId);
          intent.putExtra("productName", name);
          startActivity(intent);
        })
        .addOnFailureListener(e -> {
          Toast.makeText(getContext(), "Lỗi thêm sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
  }
}
