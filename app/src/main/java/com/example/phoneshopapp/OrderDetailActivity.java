package com.example.phoneshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phoneshopapp.adapters.OrderItemsAdapter;
import com.example.phoneshopapp.managers.OrderManager;
import com.example.phoneshopapp.models.*;
import com.example.phoneshopapp.repositories.callbacks.OrderCallback;
import com.example.phoneshopapp.repositories.callbacks.OrderCreationCallback;
import com.example.phoneshopapp.repositories.callbacks.UpdateCallback;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.tasks.TaskCompletionSource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private MaterialTextView textOrderId, textOrderDate, textOrderStatus;
    private RecyclerView recyclerViewOrderItems;
    private MaterialTextView textRecipientName, textRecipientPhone, textShippingAddress;
    private MaterialTextView textSubtotal, textShippingFee, textTotal;
    private MaterialTextView textPaymentMethod, textPaymentStatus;
    private MaterialButton btnCancelOrder, btnReorder;

    private OrderItemsAdapter orderItemsAdapter;
    private String orderId;
    private Order currentOrder;
    private OrderManager orderManager;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Initialize OrderManager
        orderManager = OrderManager.getInstance(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();

        getIntentData();
        loadOrderDetail();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        textOrderId = findViewById(R.id.textOrderId);
        textOrderDate = findViewById(R.id.textOrderDate);
        textOrderStatus = findViewById(R.id.textOrderStatus);
        recyclerViewOrderItems = findViewById(R.id.recyclerViewOrderItems);
        textRecipientName = findViewById(R.id.textRecipientName);
        textRecipientPhone = findViewById(R.id.textRecipientPhone);
        textShippingAddress = findViewById(R.id.textShippingAddress);
        textSubtotal = findViewById(R.id.textSubtotal);
        textShippingFee = findViewById(R.id.textShippingFee);
        textTotal = findViewById(R.id.textTotal);
        textPaymentMethod = findViewById(R.id.textPaymentMethod);
        textPaymentStatus = findViewById(R.id.textPaymentStatus);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        btnReorder = findViewById(R.id.btnReorder);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        orderItemsAdapter = new OrderItemsAdapter();
        recyclerViewOrderItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrderItems.setAdapter(orderItemsAdapter);
    }

    private void setupClickListeners() {
        btnCancelOrder.setOnClickListener(v -> {
            // TODO: Implement cancel order functionality
            showCancelOrderDialog();
        });

        btnReorder.setOnClickListener(v -> {
            // TODO: Implement reorder functionality
            reorderItems();
        });
    }

    private void getIntentData() {
        orderId = getIntent().getStringExtra("order_id");
    }

    private void loadOrderDetail() {
        if (orderId == null || orderId.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Show loading indicator
        showProgressBar();

        orderManager.getOrderDetails(orderId, new OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                runOnUiThread(() -> {
                    currentOrder = order;
                    displayOrderDetail(order);
                    hideProgressBar();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    hideProgressBar();
                    Toast.makeText(OrderDetailActivity.this,
                            "Lỗi tải đơn hàng: " + errorMessage,
                            Toast.LENGTH_LONG).show();
                    Log.e("OrderDetailActivity", "Error loading order: " + errorMessage);

                    // Show error state or finish activity
                    finish();
                });
            }
        });
    }

    private void showProgressBar() {
        // You can add a ProgressBar to the layout if needed
        // For now, just disable buttons
        btnCancelOrder.setEnabled(false);
        btnReorder.setEnabled(false);
    }

    private void hideProgressBar() {
        btnCancelOrder.setEnabled(true);
        btnReorder.setEnabled(true);
    }

    private void displayOrderDetail(Order order) {
        if (order == null)
            return;

        // Order basic info
        textOrderId.setText(order.getFormattedOrderId());

        if (order.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            textOrderDate.setText(sdf.format(order.getCreatedAt()));
        }

        textOrderStatus.setText(order.getStatusDisplayName());
        setStatusColor(textOrderStatus, order.getOrderStatus());

        // Order items
        if (order.getItems() != null) {
            orderItemsAdapter.updateItems(order.getItems());
        }

        // Customer info
        if (order.getCustomerInfo() != null) {
            CustomerInfo customer = order.getCustomerInfo();
            textRecipientName.setText(customer.getDisplayName());
            textRecipientPhone.setText(customer.getFormattedPhone());
            textShippingAddress.setText(customer.getAddress());
        }

        // Pricing info
        if (order.getPricing() != null) {
            PricingInfo pricing = order.getPricing();
            textSubtotal.setText(pricing.getFormattedSubtotal());
            textShippingFee.setText(pricing.getFormattedShippingFee());
            textTotal.setText(pricing.getFormattedTotal());
        }

        // Payment info
        if (order.getPaymentInfo() != null) {
            PaymentInfo payment = order.getPaymentInfo();
            textPaymentMethod.setText(payment.getMethodDisplayName());
            textPaymentStatus.setText(payment.getStatusDisplayName());
        }

        // Action buttons visibility
        updateActionButtons(order.getOrderStatus());
    }

    private void setStatusColor(MaterialTextView textView, OrderStatus status) {
        int colorRes;
        switch (status) {
            case PENDING:
                colorRes = R.color.status_pending;
                break;
            case CONFIRMED:
                colorRes = R.color.status_confirmed;
                break;
            case SHIPPING:
                colorRes = R.color.status_shipping;
                break;
            case DELIVERED:
                colorRes = R.color.status_delivered;
                break;
            case CANCELLED:
                colorRes = R.color.status_cancelled;
                break;
            default:
                colorRes = android.R.color.darker_gray;
                break;
        }
        textView.setTextColor(getResources().getColor(colorRes));
    }

    private void updateActionButtons(OrderStatus status) {
        // Show/hide buttons based on order status
        switch (status) {
            case PENDING:
            case CONFIRMED:
                btnCancelOrder.setVisibility(View.VISIBLE);
                btnReorder.setVisibility(View.GONE);
                break;
            case DELIVERED:
                btnCancelOrder.setVisibility(View.GONE);
                btnReorder.setVisibility(View.VISIBLE);
                break;
            case CANCELLED:
                btnCancelOrder.setVisibility(View.GONE);
                btnReorder.setVisibility(View.VISIBLE);
                break;
            default:
                btnCancelOrder.setVisibility(View.GONE);
                btnReorder.setVisibility(View.GONE);
                break;
        }
    }

    private void showCancelOrderDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hủy đơn hàng")
                .setMessage("Bạn có chắc muốn hủy đơn hàng này?")
                .setPositiveButton("Hủy đơn hàng", (dialog, which) -> {
                    // TODO: Implement cancel order API call
                    cancelOrder();
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void cancelOrder() {
        if (currentOrder == null)
            return;

        showProgressBar();

        orderManager.cancelOrder(orderId, new UpdateCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    hideProgressBar();
                    Toast.makeText(OrderDetailActivity.this,
                            "Đơn hàng đã được hủy thành công",
                            Toast.LENGTH_SHORT).show();

                    // Reload order to update status
                    loadOrderDetail();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    hideProgressBar();
                    Toast.makeText(OrderDetailActivity.this,
                            "Lỗi khi hủy đơn hàng: " + errorMessage,
                            Toast.LENGTH_LONG).show();
                    Log.e("OrderDetailActivity", "Error canceling order: " + errorMessage);
                });
            }
        });
    }

    private void reorderItems() {
        if (currentOrder == null || currentOrder.getItems() == null || currentOrder.getItems().isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm để đặt lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        Toast.makeText(this, "Đang thêm sản phẩm vào giỏ hàng...", Toast.LENGTH_SHORT).show();

        // Use CartRepository directly to add CartItem
        com.example.phoneshopapp.data.cart.CartRepository cartRepository = new com.example.phoneshopapp.data.cart.CartRepository();

        UserManager userManager = UserManager.getInstance(this);
        if (!userManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = userManager.getCurrentUserId();
        List<Task<Void>> addTasks = new ArrayList<>();

        // Create tasks for all cart items
        for (OrderItem item : currentOrder.getItems()) {
            TaskCompletionSource<Void> taskSource = new TaskCompletionSource<>();

            // Fetch product to get category
            ProductManager productManager = ProductManager.getInstance();
            productManager.findProductById(item.getProductId(), new ProductManager.OnSingleProductLoadedListener() {
                @Override
                public void onSuccess(Product product) {
                    // Convert OrderItem to CartItem with full properties
                    com.example.phoneshopapp.models.CartItem cartItem = new com.example.phoneshopapp.models.CartItem();
                    cartItem.setUserId(userId);
                    cartItem.setProductId(item.getProductId());
                    cartItem.setProductName(item.getProductName());
                    cartItem.setProductPrice(String.format("₫%.0f", item.getPrice()));
                    cartItem.setProductPriceValue(item.getPrice());
                    cartItem.setQuantity(item.getQuantity());
                    cartItem.setProductImageUrl(item.getImageUrl());
                    cartItem.setProductCategory(product != null ? product.getCategory() : ""); // Get category from
                                                                                               // product
                    cartItem.setProductImageResourceId(0);

                    // Copy variant information from OrderItem to CartItem
                    cartItem.setVariantId(item.getVariantId());
                    cartItem.setVariantName(item.getVariantName());
                    cartItem.setVariantShortName(item.getVariantShortName());
                    cartItem.setVariantColor(item.getVariantColor());
                    cartItem.setVariantColorHex(item.getVariantColorHex());
                    cartItem.setVariantRam(item.getVariantRam());
                    cartItem.setVariantStorage(item.getVariantStorage());

                    cartRepository.addToCart(userId, cartItem,
                            new com.example.phoneshopapp.data.cart.CartRepository.OnCartOperationListener() {
                                @Override
                                public void onSuccess() {
                                    taskSource.setResult(null);
                                }

                                @Override
                                public void onFailure(Exception error) {
                                    Log.e("OrderDetailActivity", "Failed to add item to cart: " + error.getMessage());
                                    taskSource.setException(error);
                                }
                            });
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("OrderDetailActivity", "Failed to find product for category: " + item.getProductId(), e);
                    // Continue without category
                    com.example.phoneshopapp.models.CartItem cartItem = new com.example.phoneshopapp.models.CartItem();
                    cartItem.setUserId(userId);
                    cartItem.setProductId(item.getProductId());
                    cartItem.setProductName(item.getProductName());
                    cartItem.setProductPrice(String.format("₫%.0f", item.getPrice()));
                    cartItem.setProductPriceValue(item.getPrice());
                    cartItem.setQuantity(item.getQuantity());
                    cartItem.setProductImageUrl(item.getImageUrl());
                    cartItem.setProductCategory(""); // Empty if can't find product
                    cartItem.setProductImageResourceId(0);

                    // Copy variant information
                    cartItem.setVariantId(item.getVariantId());
                    cartItem.setVariantName(item.getVariantName());
                    cartItem.setVariantShortName(item.getVariantShortName());
                    cartItem.setVariantColor(item.getVariantColor());
                    cartItem.setVariantColorHex(item.getVariantColorHex());
                    cartItem.setVariantRam(item.getVariantRam());
                    cartItem.setVariantStorage(item.getVariantStorage());

                    cartRepository.addToCart(userId, cartItem,
                            new com.example.phoneshopapp.data.cart.CartRepository.OnCartOperationListener() {
                                @Override
                                public void onSuccess() {
                                    taskSource.setResult(null);
                                }

                                @Override
                                public void onFailure(Exception error) {
                                    Log.e("OrderDetailActivity", "Failed to add item to cart: " + error.getMessage());
                                    taskSource.setException(error);
                                }
                            });
                }
            });

            addTasks.add(taskSource.getTask());
        }

        // Wait for all tasks to complete
        Tasks.whenAll(addTasks)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();

                    // Navigate to CartActivity only after all items are added
                    Intent intent = new Intent(this, com.example.phoneshopapp.CartActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Log.e("OrderDetailActivity", "Failed to add items to cart", e);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}