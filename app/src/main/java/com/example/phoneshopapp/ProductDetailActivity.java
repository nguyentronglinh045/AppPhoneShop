package com.example.phoneshopapp;

import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phoneshopapp.adapter.ReviewAdapter;
import com.example.phoneshopapp.adapters.ColorVariantAdapter;
import com.example.phoneshopapp.adapters.StorageVariantAdapter;
import com.example.phoneshopapp.data.cart.CartManager;
import com.example.phoneshopapp.data.variant.VariantRepository;
import com.example.phoneshopapp.model.Review;
import com.example.phoneshopapp.models.CartItem;
import com.example.phoneshopapp.models.ProductVariant;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

  public static final String EXTRA_PRODUCT_ID = "product_id";

  private MaterialToolbar toolbar;
  private ImageView imageProduct;
  private TextView textProductName, textProductPrice, textProductCategory, textProductDescription, textQuantity;
  private TextView textSpecScreen, textSpecProcessor, textSpecRam, textSpecStorage; // Spec TextViews
  private MaterialButton btnAddToCart, btnBuyNow, btnPlus, btnMinus;

  // Review UI components
  private TextView textAverageRating, textReviewCount, textEmptyReviews;
  private RatingBar ratingBarAverage;
  private Button buttonWriteReview;
  private RecyclerView recyclerViewReviews;
  private ReviewAdapter reviewAdapter;
  private List<Review> reviewList;

  // Variant UI components
  private LinearLayout layoutVariantSection;
  private TextView textSelectedVariant;
  private RecyclerView recyclerColorVariants;
  private RecyclerView recyclerStorageVariants;
  private LinearLayout layoutAvailability;
  private TextView textAvailability;
  private android.view.View viewAvailabilityDot;

  // Variant data
  private List<ProductVariant> allVariants;
  private ProductVariant selectedVariant;
  private String selectedColor;
  private String selectedStorage;
  private String selectedRam;
  private ColorVariantAdapter colorAdapter;
  private StorageVariantAdapter storageAdapter;
  private VariantRepository variantRepository;

  private Product product;
  private ProductManager productManager;
  private CartManager cartManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_product_detail);

    // Initialize views
    initViews();

    // Setup toolbar with back button
    setupToolbar();

    // Initialize cart manager
    cartManager = CartManager.getInstance();
    cartManager.initialize(this);

    // Load product data
    loadProductData();

    // Setup click listeners
    setupClickListeners();
  }

  private void initViews() {
    toolbar = findViewById(R.id.toolbar);
    imageProduct = findViewById(R.id.imageProduct);
    textProductName = findViewById(R.id.textProductName);
    textProductPrice = findViewById(R.id.textProductPrice);
    textProductCategory = findViewById(R.id.textProductCategory);
    textProductDescription = findViewById(R.id.textProductDescription);
    textQuantity = findViewById(R.id.textQuantity);
    btnAddToCart = findViewById(R.id.btnAddToCart);
    btnBuyNow = findViewById(R.id.btnBuyNow);
    btnPlus = findViewById(R.id.btnPlus);
    btnMinus = findViewById(R.id.btnMinus);

    // Initialize Spec TextViews
    textSpecScreen = findViewById(R.id.textSpecScreen);
    textSpecProcessor = findViewById(R.id.textSpecProcessor);
    textSpecRam = findViewById(R.id.textSpecRam);
    textSpecStorage = findViewById(R.id.textSpecStorage);

    // Initialize variant UI components
    layoutVariantSection = findViewById(R.id.layoutVariantSection);
    textSelectedVariant = findViewById(R.id.textSelectedVariant);
    recyclerColorVariants = findViewById(R.id.recyclerColorVariants);
    recyclerStorageVariants = findViewById(R.id.recyclerStorageVariants);
    layoutAvailability = findViewById(R.id.layoutAvailability);
    textAvailability = findViewById(R.id.textAvailability);
    viewAvailabilityDot = findViewById(R.id.viewAvailabilityDot);

    // Initialize review UI components
    textAverageRating = findViewById(R.id.textAverageRating);
    textReviewCount = findViewById(R.id.textReviewCount);
    ratingBarAverage = findViewById(R.id.ratingBarAverage);
    buttonWriteReview = findViewById(R.id.buttonWriteReview);
    recyclerViewReviews = findViewById(R.id.recyclerViewReviews);
    textEmptyReviews = findViewById(R.id.textEmptyReviews);

    // Initialize variant repository
    variantRepository = new VariantRepository();
    allVariants = new ArrayList<>();

    // Setup RecyclerViews
    setupReviewRecyclerView();
    setupVariantRecyclerViews();
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // Enable back button
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    // Handle back button clicks
    toolbar.setNavigationOnClickListener(v -> {
      Log.d("ProductDetail", "Back button clicked");
      onBackPressed();
    });
  }

  private void loadProductData() {
    // Lấy product ID từ Intent (now String)
    String productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);

    if (productId == null || productId.isEmpty()) {
      Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    // Lấy thông tin sản phẩm từ Firebase
    productManager = ProductManager.getInstance();
    loadProductFromFirebase(productId);
  }

  private void loadProductFromFirebase(String productId) {
    productManager.loadProductsFromFirebase(new ProductManager.OnProductsLoadedListener() {
      @Override
      public void onSuccess(List<Product> products) {
        // Tìm product theo ID trong danh sách từ Firebase
        product = findProductById(products, productId);

        if (product == null) {
          Toast.makeText(ProductDetailActivity.this, "Product not found", Toast.LENGTH_SHORT).show();
          finish();
          return;
        }

        // Hiển thị thông tin sản phẩm
        displayProductInfo();
      }

      @Override
      public void onFailure(Exception e) {
        Toast.makeText(ProductDetailActivity.this, "Failed to load product: " + e.getMessage(), Toast.LENGTH_LONG)
            .show();
        finish();
      }
    });
  }

  private Product findProductById(List<Product> products, String productId) {
    for (Product product : products) {
      if (product.getId() != null && product.getId().equals(productId)) {
        return product;
      }
    }
    return null;
  }

  private void displayProductInfo() {
    Log.d("ProductDetail", "=== DISPLAYING PRODUCT INFO ===");
    Log.d("ProductDetail", "Product Name: " + product.getName());
    Log.d("ProductDetail", "Product ID: " + product.getId());
    Log.d("ProductDetail", "Product Price: " + product.getPrice());
    Log.d("ProductDetail", "Product PriceValue: " + product.getPriceValue());
    Log.d("ProductDetail", "Product Category: " + product.getCategory());
    Log.d("ProductDetail", "Product ImageUrl: " + product.getImageUrl());
    Log.d("ProductDetail", "Product ImageResourceId: " + product.getImageResourceId());

    textProductName.setText(product.getName());
    textProductPrice.setText(product.getPrice());
    textProductCategory.setText(product.getCategory());
    textProductDescription.setText(product.getDescription());

    // Display technical specifications
    textSpecScreen.setText(product.getSpecScreen() != null ? product.getSpecScreen() : "N/A");
    textSpecProcessor.setText(product.getSpecProcessor() != null ? product.getSpecProcessor() : "N/A");
    textSpecRam.setText(product.getSpecRam() != null ? product.getSpecRam() : "N/A");
    textSpecStorage.setText(product.getSpecStorage() != null ? product.getSpecStorage() : "N/A");

    // Set product image - handle both resource ID and URL
    if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
      // Load image from URL using Glide
      com.bumptech.glide.Glide.with(this)
          .load(product.getImageUrl())
          .placeholder(R.drawable.ic_image_placeholder) // Skeleton placeholder
          .error(R.drawable.ic_image_placeholder) // Error fallback to skeleton
          .into(imageProduct);
    } else {
      // Sử dụng ảnh skeleton cho sản phẩm không có URL ảnh
      imageProduct.setImageResource(R.drawable.ic_image_placeholder);
    }

    // Update toolbar title
    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle(product.getName());
    }

    Log.d("ProductDetail", "Product info displayed successfully");

    // Load variants if product has variants
    if (product.isHasVariants()) {
      Log.d("ProductDetail", "Product has variants, loading...");
      loadProductVariants();
    } else {
      Log.d("ProductDetail", "Product has no variants");
      layoutVariantSection.setVisibility(android.view.View.GONE);
    }
  }

  private void loadProductVariants() {
    variantRepository.loadVariantsByProductId(product.getId(),
        new VariantRepository.OnVariantsLoadedListener() {
          @Override
          public void onSuccess(List<ProductVariant> variants) {
            Log.d("ProductDetail", "Loaded " + variants.size() + " variants");
            allVariants.clear();
            allVariants.addAll(variants);

            if (!variants.isEmpty()) {
              layoutVariantSection.setVisibility(android.view.View.VISIBLE);
              displayVariants();
            } else {
              layoutVariantSection.setVisibility(android.view.View.GONE);
            }
          }

          @Override
          public void onFailure(Exception e) {
            Log.e("ProductDetail", "Failed to load variants", e);
            layoutVariantSection.setVisibility(android.view.View.GONE);
            Toast.makeText(ProductDetailActivity.this,
                "Failed to load variants: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
          }
        });
  }

  private void displayVariants() {
    // Update adapters with variant data
    colorAdapter.setColors(allVariants);
    storageAdapter.setStorageOptions(allVariants);

    // Reset selection
    textSelectedVariant.setText("Please select variant");
  }

  private void updateSelectedVariant() {
    // Find the variant matching selected color and storage/RAM
    if (selectedColor == null || selectedStorage == null || selectedRam == null) {
      selectedVariant = null;
      textSelectedVariant.setText("Please select variant");
      layoutAvailability.setVisibility(android.view.View.GONE);
      return;
    }

    // Find matching variant
    for (ProductVariant variant : allVariants) {
      if (variant.getColor().equals(selectedColor) &&
          variant.getStorage().equals(selectedStorage) &&
          variant.getRam().equals(selectedRam)) {
        selectedVariant = variant;
        break;
      }
    }

    if (selectedVariant != null) {
      // Update display
      textSelectedVariant.setText("Selected: " + selectedVariant.getShortName());

      // Update specs based on selected variant
      textSpecRam.setText(selectedVariant.getRam());
      textSpecStorage.setText(selectedVariant.getStorage());

      // Update availability
      updateAvailabilityDisplay();
    } else {
      textSelectedVariant.setText("Selected combination not available");
      layoutAvailability.setVisibility(android.view.View.GONE);
    }
  }

  private void updateAvailabilityDisplay() {
    if (selectedVariant == null) {
      layoutAvailability.setVisibility(android.view.View.GONE);
      return;
    }

    layoutAvailability.setVisibility(android.view.View.VISIBLE);

    if (selectedVariant.isInStock()) {
      textAvailability.setText("In Stock (" + selectedVariant.getStockQuantity() + " available)");
      textAvailability.setTextColor(getColor(R.color.success_color));
      android.graphics.drawable.GradientDrawable dotDrawable = (android.graphics.drawable.GradientDrawable) viewAvailabilityDot
          .getBackground();
      dotDrawable.setColor(getColor(R.color.success_color));
    } else {
      textAvailability.setText("Out of Stock");
      textAvailability.setTextColor(getColor(R.color.error_color));
      android.graphics.drawable.GradientDrawable dotDrawable = (android.graphics.drawable.GradientDrawable) viewAvailabilityDot
          .getBackground();
      dotDrawable.setColor(getColor(R.color.error_color));
    }
  }

  private void setupClickListeners() {
    // Quantity controls
    if (btnPlus != null && textQuantity != null) {
      btnPlus.setOnClickListener(v -> {
        int q = getCurrentQuantity();
        if (q < 99) {
          textQuantity.setText(String.valueOf(q + 1));
        }
      });
    }

    if (btnMinus != null && textQuantity != null) {
      btnMinus.setOnClickListener(v -> {
        int q = getCurrentQuantity();
        if (q > 1) {
          textQuantity.setText(String.valueOf(q - 1));
        }
      });
    }

    // Add to Cart button
    btnAddToCart.setOnClickListener(v -> {
      Log.d("ProductDetail", "=== ADD TO CART BUTTON CLICKED ===");

      if (product == null) {
        Log.e("ProductDetail", "Product is null");
        Toast.makeText(this, "Sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
        return;
      }

      // Validate variant selection if product has variants
      if (product.isHasVariants()) {
        if (selectedVariant == null) {
          Toast.makeText(this, "Please select a variant (color, storage, RAM) before adding to cart",
              Toast.LENGTH_LONG).show();
          return;
        }

        if (!selectedVariant.isInStock()) {
          Toast.makeText(this, "Selected variant is out of stock", Toast.LENGTH_SHORT).show();
          return;
        }

        Log.d("ProductDetail", "Selected Variant: " + selectedVariant.getShortName());
        Log.d("ProductDetail", "Variant ID: " + selectedVariant.getVariantId());
      }

      Log.d("ProductDetail", "Product: " + product.getName());
      Log.d("ProductDetail", "Product ID: " + product.getId());
      Log.d("ProductDetail", "Product Price: " + product.getPrice());

      int quantity = getCurrentQuantity();
      Log.d("ProductDetail", "Quantity: " + quantity);

      // Check stock quantity for variant
      if (selectedVariant != null && quantity > selectedVariant.getStockQuantity()) {
        Toast.makeText(this, "Only " + selectedVariant.getStockQuantity() + " items available",
            Toast.LENGTH_SHORT).show();
        return;
      }

      // Disable button to prevent multiple clicks
      btnAddToCart.setEnabled(false);
      btnAddToCart.setText("Đang thêm...");

      // Debug: Verify CartManager state
      debugCartManagerState();

      // Debug: Test creating CartItem first
      debugCreateCartItem(product, quantity);

      cartManager.addToCart(product, quantity, selectedVariant, new CartManager.OnCartOperationListener() {
        @Override
        public void onSuccess(String message) {
          Log.d("ProductDetail", "Add to cart SUCCESS: " + message);
          runOnUiThread(() -> {
            Toast.makeText(ProductDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            btnAddToCart.setEnabled(true);
            btnAddToCart.setText("Add to Cart");
          });
        }

        @Override
        public void onFailure(String error) {
          Log.e("ProductDetail", "Add to cart FAILED: " + error);
          runOnUiThread(() -> {
            Toast.makeText(ProductDetailActivity.this, error, Toast.LENGTH_LONG).show();
            btnAddToCart.setEnabled(true);
            btnAddToCart.setText("Add to Cart");
          });
        }
      });
    });

    // Buy Now button
    btnBuyNow.setOnClickListener(v -> {
      if (product == null) {
        Toast.makeText(this, "Sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
        return;
      }

      // Validate variant selection if product has variants
      if (product.isHasVariants()) {
        if (selectedVariant == null) {
          Toast.makeText(this, "Please select a variant (color, storage, RAM) before buying",
              Toast.LENGTH_LONG).show();
          return;
        }

        if (!selectedVariant.isInStock()) {
          Toast.makeText(this, "Selected variant is out of stock", Toast.LENGTH_SHORT).show();
          return;
        }
      }

      int quantity = getCurrentQuantity();

      // Check stock quantity for variant
      if (selectedVariant != null && quantity > selectedVariant.getStockQuantity()) {
        Toast.makeText(this, "Only " + selectedVariant.getStockQuantity() + " items available",
            Toast.LENGTH_SHORT).show();
        return;
      }

      // First add to cart, then proceed to checkout
      btnBuyNow.setEnabled(false);
      btnBuyNow.setText("Đang xử lý...");

      cartManager.addToCart(product, quantity, selectedVariant, new CartManager.OnCartOperationListener() {
        @Override
        public void onSuccess(String message) {
          runOnUiThread(() -> {
            Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng. Chuyển đến thanh toán...",
                Toast.LENGTH_SHORT).show();
            btnBuyNow.setEnabled(true);
            btnBuyNow.setText("Buy Now");
            // TODO: Navigate to checkout/cart activity
          });
        }

        @Override
        public void onFailure(String error) {
          runOnUiThread(() -> {
            Toast.makeText(ProductDetailActivity.this, error, Toast.LENGTH_LONG).show();
            btnBuyNow.setEnabled(true);
            btnBuyNow.setText("Buy Now");
          });
        }
      });
    });

    // Write Review button
    buttonWriteReview.setOnClickListener(v -> showWriteReviewDialog());
  }

  private void setupReviewRecyclerView() {
    reviewList = new ArrayList<>();
    reviewAdapter = new ReviewAdapter(this, reviewList);
    recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
    recyclerViewReviews.setAdapter(reviewAdapter);

    // Load sample reviews for demo
    loadSampleReviews();
  }

  private void setupVariantRecyclerViews() {
    // Setup color variants RecyclerView
    colorAdapter = new ColorVariantAdapter((color, position) -> {
      selectedColor = color;
      updateSelectedVariant();
    });
    recyclerColorVariants.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    recyclerColorVariants.setAdapter(colorAdapter);

    // Setup storage variants RecyclerView
    storageAdapter = new StorageVariantAdapter((storage, ram, position) -> {
      selectedStorage = storage;
      selectedRam = ram;
      updateSelectedVariant();
    });
    recyclerStorageVariants.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    recyclerStorageVariants.setAdapter(storageAdapter);
  }

  private void loadSampleReviews() {
    // Sample reviews for demonstration
    reviewList.clear();

    Review review1 = new Review("user1", "Nguyễn Văn A", "product1", 5.0f, "Sản phẩm rất tốt, chất lượng cao!");
    Review review2 = new Review("user2", "Trần Thị B", "product1", 4.0f, "Đẹp và tiện dụng, giao hàng nhanh.");
    Review review3 = new Review("user3", "Lê Văn C", "product1", 4.5f, "Giá hợp lý, sẽ mua lại lần sau.");

    reviewList.add(review1);
    reviewList.add(review2);
    reviewList.add(review3);

    updateReviewSummary();
    updateReviewsDisplay();
  }

  private void updateReviewSummary() {
    if (reviewList.isEmpty()) {
      textAverageRating.setText("0.0");
      textReviewCount.setText("(0 đánh giá)");
      ratingBarAverage.setRating(0);
      return;
    }

    // Calculate average rating
    float totalRating = 0;
    for (Review review : reviewList) {
      totalRating += review.getRating();
    }
    float averageRating = totalRating / reviewList.size();

    textAverageRating.setText(String.format("%.1f", averageRating));
    textReviewCount.setText(String.format("(%d đánh giá)", reviewList.size()));
    ratingBarAverage.setRating(averageRating);
  }

  private void updateReviewsDisplay() {
    if (reviewList.isEmpty()) {
      recyclerViewReviews.setVisibility(android.view.View.GONE);
      textEmptyReviews.setVisibility(android.view.View.VISIBLE);
    } else {
      recyclerViewReviews.setVisibility(android.view.View.VISIBLE);
      textEmptyReviews.setVisibility(android.view.View.GONE);
      reviewAdapter.updateReviews(reviewList);
    }
  }

  private void showWriteReviewDialog() {
    Dialog dialog = new Dialog(this);
    dialog.setContentView(R.layout.dialog_write_review);
    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    RatingBar ratingBarInput = dialog.findViewById(R.id.ratingBarInput);
    EditText editTextComment = dialog.findViewById(R.id.editTextComment);
    Button buttonSubmitReview = dialog.findViewById(R.id.buttonSubmitReview);

    buttonSubmitReview.setOnClickListener(v -> {
      float rating = ratingBarInput.getRating();
      String comment = editTextComment.getText().toString().trim();

      if (rating == 0) {
        Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
        return;
      }

      if (comment.isEmpty()) {
        Toast.makeText(this, "Vui lòng nhập nhận xét", Toast.LENGTH_SHORT).show();
        return;
      }

      // Add new review (in real app, save to Firebase)
      UserManager userManager = UserManager.getInstance(this);
      String userName = userManager.isLoggedIn() ? userManager.getCurrentUserId() : "Khách hàng";

      Review newReview = new Review("currentUser", userName, product.getId(), rating, comment);
      reviewList.add(0, newReview); // Add to top

      updateReviewSummary();
      updateReviewsDisplay();

      Toast.makeText(this, "Đánh giá đã được gửi thành công!", Toast.LENGTH_SHORT).show();
      dialog.dismiss();
    });

    dialog.show();
  }

  private int getCurrentQuantity() {
    try {
      String txt = textQuantity != null ? textQuantity.getText().toString().trim() : "1";
      int q = Integer.parseInt(txt);
      return Math.max(1, Math.min(q, 99));
    } catch (Exception e) {
      return 1;
    }
  }

  // Debug method to check CartManager state
  private void debugCartManagerState() {
    Log.d("ProductDetail", "=== DEBUGGING CART MANAGER STATE ===");
    if (cartManager == null) {
      Log.e("ProductDetail", "CartManager is NULL!");
      return;
    }

    Log.d("ProductDetail", "CartManager instance: " + cartManager.toString());

    // Check UserManager state
    UserManager userManager = UserManager.getInstance(this);
    Log.d("ProductDetail", "UserManager isLoggedIn: " + userManager.isLoggedIn());
    if (userManager.isLoggedIn()) {
      Log.d("ProductDetail", "Current User ID: " + userManager.getCurrentUserId());
    }
  }

  // Debug method to test creating CartItem
  private void debugCreateCartItem(Product product, int quantity) {
    Log.d("ProductDetail", "=== DEBUGGING CART ITEM CREATION ===");
    try {
      UserManager userManager = UserManager.getInstance(this);
      String userId = userManager.getCurrentUserId();

      Log.d("ProductDetail", "About to create CartItem with:");
      Log.d("ProductDetail", "- UserId: " + userId);
      Log.d("ProductDetail", "- Product: " + (product != null ? product.getName() : "NULL"));
      Log.d("ProductDetail", "- Quantity: " + quantity);

      if (product != null && userId != null) {
        CartItem testItem = new CartItem(userId, product, quantity);
        Log.d("ProductDetail", "✅ CartItem created successfully in debug test");
        Log.d("ProductDetail", "CartItem productName: " + testItem.getProductName());
        Log.d("ProductDetail", "CartItem userId: " + testItem.getUserId());
      } else {
        Log.e("ProductDetail", "❌ Cannot create CartItem - product or userId is null");
      }
    } catch (Exception e) {
      Log.e("ProductDetail", "❌ Error creating CartItem in debug test", e);
    }
  }

  @Override
  public boolean onSupportNavigateUp() {
    Log.d("ProductDetail", "onSupportNavigateUp called");
    onBackPressed();
    return true;
  }

  @Override
  public void onBackPressed() {
    Log.d("ProductDetail", "onBackPressed called - returning to previous activity");
    super.onBackPressed();
    // This will return to the previous activity (MainActivity or wherever user came
    // from)
    finish();
  }
}