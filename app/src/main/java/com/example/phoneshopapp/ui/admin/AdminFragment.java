package com.example.phoneshopapp.ui.admin;

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
import com.example.phoneshopapp.Product;
import com.example.phoneshopapp.R;
import java.util.ArrayList;
import java.util.List;

public class AdminFragment extends Fragment {
  private RecyclerView recyclerProducts;
  private Button btnAddProduct;
  private ProductAdminAdapter adapter;
  private List<Product> productList = new ArrayList<>();

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_admin, container, false);
    recyclerProducts = root.findViewById(R.id.recyclerProducts);
    btnAddProduct = root.findViewById(R.id.btnAddProduct);

    adapter = new ProductAdminAdapter(productList, this::onEditProduct, this::onDeleteProduct);
    recyclerProducts.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerProducts.setAdapter(adapter);

    btnAddProduct.setOnClickListener(v -> onAddProduct());

    loadProducts();
    return root;
  }

  private void loadProducts() {
    com.example.phoneshopapp.ProductManager.getInstance()
        .loadProductsFromFirebase(new com.example.phoneshopapp.ProductManager.OnProductsLoadedListener() {
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
    com.example.phoneshopapp.ProductManager.getInstance()
        .forceRefreshFromFirebase(new com.example.phoneshopapp.ProductManager.OnProductsLoadedListener() {
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
    // Tạo dialog thêm sản phẩm mới
    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
    builder.setView(dialogView);

    // Lấy các view từ dialog
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

    // Xử lý nút Hủy
    btnCancel.setOnClickListener(v -> dialog.dismiss());

    // Xử lý nút Lưu
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

      // Tạo ID tự động cho sản phẩm mới
      generateProductIdAndSave(name, price, brand, category, description, imageUrl,
          checkFeatured.isChecked(), checkBestDeal.isChecked(), dialog);
    });

    dialog.show();
  }

  private void onEditProduct(Product product) {
    // Tạo dialog chỉnh sửa sản phẩm
    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_product, null);
    builder.setView(dialogView);

    // Lấy các view từ dialog
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

    // Pre-fill thông tin hiện tại
    editName.setText(product.getName());
    editPrice.setText(product.getPrice());
    editBrand.setText(product.getBrand());
    editCategory.setText(product.getCategory());
    editDescription.setText(product.getDescription());
    editImageUrl.setText(product.getImageUrl() != null ? product.getImageUrl() : "");
    checkFeatured.setChecked(product.isFeatured());
    checkBestDeal.setChecked(product.isBestDeal());

    android.app.AlertDialog dialog = builder.create();

    // Xử lý nút Hủy
    btnCancel.setOnClickListener(v -> dialog.dismiss());

    // Xử lý nút Lưu
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

      // Cập nhật thông tin sản phẩm
      product.setName(name);
      product.setPrice(price);
      product.setBrand(brand);
      product.setCategory(category);
      product.setDescription(description);
      product.setImageUrl(imageUrl);
      product.setFeatured(checkFeatured.isChecked());
      product.setBestDeal(checkBestDeal.isChecked());

      // Cập nhật lên Firestore
      com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore
          .getInstance();
      db.collection("PhoneDB")
          .document(product.getId())
          .set(product)
          .addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Đã cập nhật sản phẩm: " + product.getName(), Toast.LENGTH_SHORT).show();
            forceRefreshProducts(); // Force refresh để đảm bảo hiển thị thay đổi
            dialog.dismiss();
          })
          .addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Lỗi cập nhật sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
          });
    });

    dialog.show();
  }

  private void onDeleteProduct(Product product) {
    // Dialog xác nhận xóa sản phẩm
    new androidx.appcompat.app.AlertDialog.Builder(getContext())
        .setTitle("Xóa sản phẩm")
        .setMessage("Bạn có chắc chắn muốn xóa sản phẩm \"" + product.getName() + "\"?")
        .setPositiveButton("Xóa", (dialog, which) -> {
          // Xóa sản phẩm khỏi Firestore
          com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore
              .getInstance();
          db.collection("PhoneDB")
              .document(product.getId())
              .delete()
              .addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Đã xóa sản phẩm: " + product.getName(), Toast.LENGTH_SHORT).show();
                forceRefreshProducts(); // Force refresh để đảm bảo hiển thị thay đổi
              })
              .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Lỗi xóa sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
              });
        })
        .setNegativeButton("Hủy", null)
        .show();
  }

  private void generateProductIdAndSave(String name, String price, String brand, String category,
      String description, String imageUrl, boolean isFeatured,
      boolean isBestDeal, android.app.AlertDialog dialog) {
    // Lấy Firestore instance
    com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

    // Generate unique document ID using timestamp
    long timestamp = System.currentTimeMillis();
    String newId = "product-" + timestamp;

    // Tạo sản phẩm mới với ID đã generate
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
    newProduct.setStockQuantity(10); // Mặc định có 10 sản phẩm

    // Lưu sản phẩm vào Firestore với document ID = product ID
    db.collection("PhoneDB")
        .document(newId)
        .set(newProduct)
        .addOnSuccessListener(aVoid -> {
          Toast.makeText(getContext(), "Đã thêm sản phẩm: " + name, Toast.LENGTH_SHORT).show();
          // Force refresh để đảm bảo hiển thị ngay lập tức
          forceRefreshProducts();
          dialog.dismiss();
        })
        .addOnFailureListener(e -> {
          Toast.makeText(getContext(), "Lỗi thêm sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
  }
}
