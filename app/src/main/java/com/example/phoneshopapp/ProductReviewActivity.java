package com.example.phoneshopapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.phoneshopapp.managers.ReviewManager;
import com.example.phoneshopapp.models.Review;
import com.example.phoneshopapp.repositories.callbacks.BooleanCallback;
import com.example.phoneshopapp.repositories.callbacks.ReviewCallback;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.Date;

/**
 * Activity cho phép user đánh giá sản phẩm sau khi đơn hàng đã được giao (DELIVERED)
 * 
 * IMPORTANT RULES:
 * - Mỗi đơn hàng chỉ được đánh giá 1 LẦN DUY NHẤT
 * - Review là PERMANENT (không sửa/xóa được)
 * - Phải kiểm tra orderId chưa được review trước khi cho phép submit
 * 
 * Flow:
 * 1. Check order đã được review chưa khi onCreate
 * 2. Nếu đã review → show toast và finish() activity
 * 3. Nếu chưa review → cho phép user nhập đánh giá
 * 4. Validate input trước khi submit
 * 5. Submit review và update Order.hasReview = true
 */
public class ProductReviewActivity extends AppCompatActivity {

    private static final String TAG = "ProductReviewActivity";

    // Intent extras keys
    public static final String EXTRA_ORDER_ID = "order_id";
    public static final String EXTRA_PRODUCT_ID = "product_id";
    public static final String EXTRA_PRODUCT_NAME = "product_name";
    public static final String EXTRA_PRODUCT_IMAGE = "product_image";
    public static final String EXTRA_VARIANT_ID = "variant_id";
    public static final String EXTRA_VARIANT_NAME = "variant_name";
    public static final String EXTRA_VARIANT_COLOR = "variant_color";
    public static final String EXTRA_VARIANT_RAM = "variant_ram";
    public static final String EXTRA_VARIANT_STORAGE = "variant_storage";

    // Data from Intent
    private String orderId;
    private String productId;
    private String productName;
    private String productImage;
    private String variantId;
    private String variantName;
    private String variantColor;
    private String variantRam;
    private String variantStorage;

    // Managers
    private ReviewManager reviewManager;
    private UserManager userManager;

    // UI Components
    private MaterialToolbar toolbar;
    private ImageView imageProduct;
    private TextView textProductName;
    private TextView textVariantInfo;
    private RatingBar ratingBar;
    private EditText editTextComment;
    private MaterialButton btnSubmitReview;
    private MaterialButton btnAddImages;
    private FrameLayout loadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_review);

        // Initialize managers
        reviewManager = ReviewManager.getInstance(this);
        userManager = UserManager.getInstance(this);

        // Initialize views
        initViews();

        // Load data from Intent
        loadIntentData();

        // Display product info
        displayProductInfo();

        // Setup listeners
        setupListeners();

        // QUAN TRỌNG: Kiểm tra đơn hàng đã review chưa
        checkOrderReviewStatus();
    }

    /**
     * Initialize all view components
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imageProduct = findViewById(R.id.imageProduct);
        textProductName = findViewById(R.id.textProductName);
        textVariantInfo = findViewById(R.id.textVariantInfo);
        ratingBar = findViewById(R.id.ratingBar);
        editTextComment = findViewById(R.id.editTextComment);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        btnAddImages = findViewById(R.id.btnAddImages);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Đánh giá sản phẩm");
        }
    }

    /**
     * Load data from Intent extras
     */
    private void loadIntentData() {
        if (getIntent() != null) {
            orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
            productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
            productName = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);
            productImage = getIntent().getStringExtra(EXTRA_PRODUCT_IMAGE);
            variantId = getIntent().getStringExtra(EXTRA_VARIANT_ID);
            variantName = getIntent().getStringExtra(EXTRA_VARIANT_NAME);
            variantColor = getIntent().getStringExtra(EXTRA_VARIANT_COLOR);
            variantRam = getIntent().getStringExtra(EXTRA_VARIANT_RAM);
            variantStorage = getIntent().getStringExtra(EXTRA_VARIANT_STORAGE);

            Log.d(TAG, "Loaded intent data - OrderId: " + orderId + ", ProductId: " + productId);
        }
    }

    /**
     * Display product information in UI
     */
    private void displayProductInfo() {
        // Product name
        if (productName != null) {
            textProductName.setText(productName);
        }

        // Product image
        if (productImage != null && !productImage.isEmpty()) {
            Glide.with(this)
                    .load(productImage)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(imageProduct);
        }

        // Variant info
        String variantInfoText = buildVariantInfoText();
        if (variantInfoText != null && !variantInfoText.isEmpty()) {
            textVariantInfo.setText(variantInfoText);
            textVariantInfo.setVisibility(View.VISIBLE);
        } else {
            textVariantInfo.setVisibility(View.GONE);
        }
    }

    /**
     * Build variant info text from variant details
     * Format: "Màu sắc - RAM/Storage"
     * Example: "Titan Tự nhiên - 8GB/256GB"
     */
    private String buildVariantInfoText() {
        if (variantName != null && !variantName.isEmpty()) {
            return variantName;
        }

        // Build from components if variantName not available
        StringBuilder sb = new StringBuilder();

        if (variantColor != null && !variantColor.isEmpty()) {
            sb.append(variantColor);
        }

        if (variantRam != null && variantStorage != null) {
            if (sb.length() > 0) {
                sb.append(" - ");
            }
            sb.append(variantRam).append("/").append(variantStorage);
        }

        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * Setup click listeners
     */
    private void setupListeners() {
        // Toolbar back button
        toolbar.setNavigationOnClickListener(v -> finish());

        // Submit review button
        btnSubmitReview.setOnClickListener(v -> submitReview());

        // Add images button (optional - Phase 2)
        btnAddImages.setOnClickListener(v -> {
            // TODO: Implement image picker
            Toast.makeText(this, "Tính năng sẽ có trong phiên bản tiếp theo", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * QUAN TRỌNG: Kiểm tra đơn hàng đã review chưa khi activity mở
     * Nếu đã review → finish() activity ngay lập tức
     */
    private void checkOrderReviewStatus() {
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Thiếu thông tin đơn hàng", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        showLoading(true);

        reviewManager.checkCanReview(orderId, new BooleanCallback() {
            @Override
            public void onResult(boolean canReview) {
                runOnUiThread(() -> {
                    showLoading(false);

                    if (!canReview) {
                        // Đã review rồi, không cho submit
                        Toast.makeText(ProductReviewActivity.this,
                                "Đơn hàng này đã được đánh giá rồi",
                                Toast.LENGTH_LONG).show();
                        finish(); // Đóng activity
                    } else {
                        Log.d(TAG, "Order can be reviewed: " + orderId);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ProductReviewActivity.this,
                            "Lỗi: " + error,
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error checking order review status: " + error);
                });
            }
        });
    }

    /**
     * Submit review to Firebase
     * Flow:
     * 1. Validate input (UI level)
     * 2. Create Review object
     * 3. Submit via ReviewManager (includes validation & check orderId)
     * 4. Handle success/error
     */
    private void submitReview() {
        // 1. Validate input
        if (!validateInput()) {
            return;
        }

        // 2. Create Review object
        Review review = createReviewObject();
        if (review == null) {
            Toast.makeText(this, "Không thể tạo đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Show loading
        showLoading(true);
        btnSubmitReview.setEnabled(false);
        btnSubmitReview.setText("Đang gửi...");

        // 4. Submit via ReviewManager
        reviewManager.submitReview(review, new ReviewCallback() {
            @Override
            public void onSuccess(Review createdReview) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ProductReviewActivity.this,
                            "Đánh giá đã được gửi thành công!",
                            Toast.LENGTH_SHORT).show();

                    Log.d(TAG, "Review submitted successfully for orderId: " + orderId);

                    // Close activity and return to previous screen
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    btnSubmitReview.setEnabled(true);
                    btnSubmitReview.setText("Gửi đánh giá");

                    Toast.makeText(ProductReviewActivity.this,
                            error,
                            Toast.LENGTH_LONG).show();

                    Log.e(TAG, "Error submitting review: " + error);
                });
            }
        });
    }

    /**
     * Validate user input
     * Check rating and comment before submitting
     * 
     * @return true if valid, false otherwise
     */
    private boolean validateInput() {
        // Validate rating
        if (ratingBar.getRating() == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate comment
        String comment = editTextComment.getText().toString().trim();
        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nhận xét", Toast.LENGTH_SHORT).show();
            editTextComment.requestFocus();
            return false;
        }

        if (comment.length() < reviewManager.getMinCommentLength()) {
            Toast.makeText(this,
                    String.format("Nhận xét quá ngắn (tối thiểu %d ký tự)",
                            reviewManager.getMinCommentLength()),
                    Toast.LENGTH_SHORT).show();
            editTextComment.requestFocus();
            return false;
        }

        if (comment.length() > reviewManager.getMaxCommentLength()) {
            Toast.makeText(this,
                    String.format("Nhận xét quá dài (tối đa %d ký tự)",
                            reviewManager.getMaxCommentLength()),
                    Toast.LENGTH_SHORT).show();
            editTextComment.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Create Review object from user input
     * 
     * @return Review object or null if data is invalid
     */
    private Review createReviewObject() {
        // Get current user info
        String userId = userManager.getCurrentUserId();
        String userName = userManager.getUsername();

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            return null;
        }

        // Create Review object
        Review review = new Review();

        // Order info
        review.setOrderId(orderId);

        // User info
        review.setUserId(userId);
        review.setUserName(userName);

        // Product info
        review.setProductId(productId);
        review.setProductName(productName);

        // Variant info
        review.setVariantId(variantId);
        review.setVariantName(buildVariantInfoText());
        review.setVariantColor(variantColor);
        review.setVariantRam(variantRam);
        review.setVariantStorage(variantStorage);

        // Review content
        review.setRating(ratingBar.getRating());
        review.setComment(editTextComment.getText().toString().trim());

        // Metadata
        review.setVerifiedPurchase(true); // Luôn true vì phải mua mới đánh giá được
        review.setCreatedAt(new Date());
        review.setUpdatedAt(new Date());

        // Review images (optional - Phase 2)
        // review.setReviewImages(selectedImageUrls);

        return review;
    }

    /**
     * Show/hide loading overlay
     */
    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
