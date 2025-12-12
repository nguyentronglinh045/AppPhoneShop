package com.example.phoneshopapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phoneshopapp.data.variant.VariantRepository;
import com.example.phoneshopapp.models.ProductVariant;
import com.example.phoneshopapp.ui.admin.VariantAdapter;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for managing product variants (add, edit, delete)
 */
public class ManageVariantsActivity extends AppCompatActivity {
  private String productId;
  private String productName;
  private RecyclerView recyclerVariants;
  private Button btnAddVariant;
  private TextView textProductName;
  private VariantAdapter adapter;
  private List<ProductVariant> variantList = new ArrayList<>();
  private VariantRepository variantRepository;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_manage_variants);

    // Get product info from intent
    productId = getIntent().getStringExtra("productId");
    productName = getIntent().getStringExtra("productName");

    // Setup toolbar
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle("Quản lý phiên bản");
    }

    // Initialize views
    textProductName = findViewById(R.id.textProductName);
    recyclerVariants = findViewById(R.id.recyclerVariants);
    btnAddVariant = findViewById(R.id.btnAddVariant);

    textProductName.setText("Phiên bản của: " + productName);

    // Initialize repository
    variantRepository = new VariantRepository();

    // Setup RecyclerView
    adapter = new VariantAdapter(variantList, this::onEditVariant, this::onDeleteVariant);
    recyclerVariants.setLayoutManager(new LinearLayoutManager(this));
    recyclerVariants.setAdapter(adapter);

    // Setup add button
    btnAddVariant.setOnClickListener(v -> onAddVariant());

    // Load variants
    loadVariants();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void loadVariants() {
    variantRepository.loadVariantsByProductId(productId, new VariantRepository.OnVariantsLoadedListener() {
      @Override
      public void onSuccess(List<ProductVariant> variants) {
        variantList.clear();
        variantList.addAll(variants);
        adapter.notifyDataSetChanged();

        if (variants.isEmpty()) {
          Toast.makeText(ManageVariantsActivity.this, "Không tìm thấy phiên bản nào. Vui lòng thêm ít nhất một phiên bản.", Toast.LENGTH_LONG)
              .show();
        }
      }

      @Override
      public void onFailure(Exception e) {
        Toast.makeText(ManageVariantsActivity.this, "Lỗi tải phiên bản: " + e.getMessage(), Toast.LENGTH_SHORT)
            .show();
      }
    });
  }

  private void onAddVariant() {
    // Create dialog for adding new variant
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_variant, null);
    builder.setView(dialogView);

    // Get views from dialog
    TextInputEditText editColor = dialogView.findViewById(R.id.editColor);
    TextInputEditText editColorHex = dialogView.findViewById(R.id.editColorHex);
    Spinner spinnerRam = dialogView.findViewById(R.id.spinnerRam);
    Spinner spinnerStorage = dialogView.findViewById(R.id.spinnerStorage);
    TextInputEditText editSku = dialogView.findViewById(R.id.editSku);
    TextInputEditText editStock = dialogView.findViewById(R.id.editStock);
    CheckBox checkAvailable = dialogView.findViewById(R.id.checkAvailable);
    Button btnCancel = dialogView.findViewById(R.id.btnCancel);
    Button btnSave = dialogView.findViewById(R.id.btnSave);

    AlertDialog dialog = builder.create();

    // Set up default values after color is entered
    editColor.setOnFocusChangeListener((v, hasFocus) -> {
      if (!hasFocus && !editColor.getText().toString().isEmpty()) {
        String color = editColor.getText().toString().trim();
        // Auto-suggest SKU
        String brandModel = "";
        if (productName != null && !productName.isEmpty()) {
          brandModel = productName.replaceAll("\\s+", "-").toUpperCase();
        }
        String ramValue = spinnerRam.getSelectedItem().toString();
        String storageValue = spinnerStorage.getSelectedItem().toString();
        String suggestedSku = brandModel + "-" + color.toUpperCase() + "-" + ramValue + "-" + storageValue;
        editSku.setText(suggestedSku);
      }
    });

    // Handle Cancel button
    btnCancel.setOnClickListener(v -> dialog.dismiss());

    // Handle Save button
    btnSave.setOnClickListener(v -> {
      String color = editColor.getText().toString().trim();
      String colorHex = editColorHex.getText().toString().trim();
      String ram = spinnerRam.getSelectedItem().toString();
      String storage = spinnerStorage.getSelectedItem().toString();
      String sku = editSku.getText().toString().trim();
      String stockStr = editStock.getText().toString().trim();
      boolean isAvailable = checkAvailable.isChecked();

      // Validation
      if (color.isEmpty()) {
        Toast.makeText(this, "Vui lòng nhập màu sắc", Toast.LENGTH_SHORT).show();
        return;
      }

      int stock = 0;
      try {
        stock = Integer.parseInt(stockStr);
      } catch (NumberFormatException e) {
        // Default to 0
      }

      // Create variant
      ProductVariant variant = new ProductVariant();
      variant.setProductId(productId);
      variant.setColor(color);
      variant.setColorHex(colorHex.isEmpty() ? "#CCCCCC" : colorHex);
      variant.setRam(ram);
      variant.setStorage(storage);
      variant.setSku(sku.isEmpty() ? null : sku);
      variant.setStockQuantity(stock);
      variant.setAvailable(isAvailable);

      // Generate display names
      String shortName = color + " " + ram + "/" + storage;
      String name = productName + " - " + shortName;
      variant.setShortName(shortName);
      variant.setName(name);

      // Save to Firestore
      variantRepository.createVariant(variant, new VariantRepository.OnVariantSavedListener() {
        @Override
        public void onSuccess() {
          Toast.makeText(ManageVariantsActivity.this, "Đã thêm phiên bản thành công", Toast.LENGTH_SHORT).show();
          loadVariants();
          dialog.dismiss();
        }

        @Override
        public void onFailure(Exception e) {
          Toast.makeText(ManageVariantsActivity.this, "Lỗi thêm phiên bản: " + e.getMessage(), Toast.LENGTH_SHORT)
              .show();
        }
      });
    });

    dialog.show();
  }

  private void onEditVariant(ProductVariant variant) {
    // Create dialog for editing variant
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_variant, null);
    builder.setView(dialogView);

    // Get views from dialog
    TextInputEditText editColor = dialogView.findViewById(R.id.editColor);
    TextInputEditText editColorHex = dialogView.findViewById(R.id.editColorHex);
    Spinner spinnerRam = dialogView.findViewById(R.id.spinnerRam);
    Spinner spinnerStorage = dialogView.findViewById(R.id.spinnerStorage);
    TextInputEditText editSku = dialogView.findViewById(R.id.editSku);
    TextInputEditText editStock = dialogView.findViewById(R.id.editStock);
    CheckBox checkAvailable = dialogView.findViewById(R.id.checkAvailable);
    Button btnCancel = dialogView.findViewById(R.id.btnCancel);
    Button btnSave = dialogView.findViewById(R.id.btnSave);

    // Pre-fill current information
    editColor.setText(variant.getColor());
    editColorHex.setText(variant.getColorHex());

    // Set correct spinner position for RAM and Storage
    setSpinnerToValue(spinnerRam, variant.getRam());
    setSpinnerToValue(spinnerStorage, variant.getStorage());

    editSku.setText(variant.getSku() != null ? variant.getSku() : "");
    editStock.setText(String.valueOf(variant.getStockQuantity()));
    checkAvailable.setChecked(variant.isAvailable());

    AlertDialog dialog = builder.create();

    // Handle Cancel button
    btnCancel.setOnClickListener(v -> dialog.dismiss());

    // Handle Save button
    btnSave.setOnClickListener(v -> {
      String color = editColor.getText().toString().trim();
      String colorHex = editColorHex.getText().toString().trim();
      String ram = spinnerRam.getSelectedItem().toString();
      String storage = spinnerStorage.getSelectedItem().toString();
      String sku = editSku.getText().toString().trim();
      String stockStr = editStock.getText().toString().trim();
      boolean isAvailable = checkAvailable.isChecked();

      // Validation
      if (color.isEmpty()) {
        Toast.makeText(this, "Vui lòng điền trường màu sắc", Toast.LENGTH_SHORT).show();
        return;
      }

      int stock = 0;
      try {
        stock = Integer.parseInt(stockStr);
      } catch (NumberFormatException e) {
        // Default to 0
      }

      // Update variant
      variant.setColor(color);
      variant.setColorHex(colorHex.isEmpty() ? "#CCCCCC" : colorHex);
      variant.setRam(ram);
      variant.setStorage(storage);
      variant.setSku(sku.isEmpty() ? null : sku);
      variant.setStockQuantity(stock);
      variant.setAvailable(isAvailable);

      // Update display names
      String shortName = color + " " + ram + "/" + storage;
      String name = productName + " - " + shortName;
      variant.setShortName(shortName);
      variant.setName(name);

      // Update in Firestore
      variantRepository.updateVariant(variant, new VariantRepository.OnVariantSavedListener() {
        @Override
        public void onSuccess() {
          Toast.makeText(ManageVariantsActivity.this, "Đã cập nhật phiên bản sản phẩm thành công", Toast.LENGTH_SHORT).show();
          loadVariants();
          dialog.dismiss();
        }

        @Override
        public void onFailure(Exception e) {
          Toast.makeText(ManageVariantsActivity.this, "Lỗi cập nhật phiên bản sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT)
              .show();
        }
      });
    });

    dialog.show();
  }

  /**
   * Helper method to set spinner to the correct value
   * 
   * @param spinner The spinner to set
   * @param value   The value to set the spinner to
   */
  private void setSpinnerToValue(Spinner spinner, String value) {
    ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
    for (int i = 0; i < adapter.getCount(); i++) {
      if (adapter.getItem(i).toString().equals(value)) {
        spinner.setSelection(i);
        break;
      }
    }
  }

  private void onDeleteVariant(ProductVariant variant) {
    // Confirmation dialog
    new AlertDialog.Builder(this)
        .setTitle("Xóa phiên bản")
        .setMessage("Bạn có chắc chắn muốn xóa phiên bản này không?")
        .setPositiveButton("Xóa", (dialog, which) -> {
          variantRepository.deleteVariant(variant.getVariantId(), new VariantRepository.OnVariantDeletedListener() {
            @Override
            public void onSuccess() {
              Toast.makeText(ManageVariantsActivity.this, "Đã xóa phiên bản thành công", Toast.LENGTH_SHORT).show();
              loadVariants();
            }

            @Override
            public void onFailure(Exception e) {
              Toast.makeText(ManageVariantsActivity.this, "Lỗi xóa phiên bản: " + e.getMessage(),
                  Toast.LENGTH_SHORT).show();
            }
          });
        })
        .setNegativeButton("Hủy", null)
        .show();
  }
}
