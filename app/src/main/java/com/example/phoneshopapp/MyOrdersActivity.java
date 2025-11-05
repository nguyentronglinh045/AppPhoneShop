package com.example.phoneshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.phoneshopapp.adapters.OrdersAdapter;
import com.example.phoneshopapp.managers.OrderManager;
import com.example.phoneshopapp.managers.ReviewManager;
import com.example.phoneshopapp.models.Order;
import com.example.phoneshopapp.models.OrderStatus;
import com.example.phoneshopapp.repositories.callbacks.BooleanCallback;
import com.example.phoneshopapp.repositories.callbacks.OrderListCallback;
import com.example.phoneshopapp.UserManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import java.util.ArrayList;
import java.util.List;

public class MyOrdersActivity extends AppCompatActivity implements OrdersAdapter.OnOrderClickListener {

    private MaterialToolbar toolbar;
    private TabLayout tabLayoutOrderStatus;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewOrders;
    private MaterialTextView textEmptyState;

    private OrdersAdapter ordersAdapter;
    private List<Order> allOrders = new ArrayList<>();
    private OrderStatus currentFilter = null; // null means "All"
    private OrderManager orderManager;
    private ReviewManager reviewManager;  // Thêm ReviewManager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        // Initialize managers
        orderManager = OrderManager.getInstance(this);
        reviewManager = ReviewManager.getInstance(this);  // Init ReviewManager

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupTabLayout();
        setupSwipeRefresh();

        loadOrders();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayoutOrderStatus = findViewById(R.id.tabLayoutOrderStatus);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        textEmptyState = findViewById(R.id.textEmptyState);
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
        ordersAdapter = new OrdersAdapter(this);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setAdapter(ordersAdapter);
    }

    private void setupTabLayout() {
        tabLayoutOrderStatus.addTab(tabLayoutOrderStatus.newTab().setText("Tất cả"));
        tabLayoutOrderStatus.addTab(tabLayoutOrderStatus.newTab().setText("Chờ xác nhận"));
        tabLayoutOrderStatus.addTab(tabLayoutOrderStatus.newTab().setText("Đang giao"));
        tabLayoutOrderStatus.addTab(tabLayoutOrderStatus.newTab().setText("Đã giao"));
        tabLayoutOrderStatus.addTab(tabLayoutOrderStatus.newTab().setText("Đã hủy"));

        tabLayoutOrderStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterOrdersByStatus(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadOrders();
        });
    }

    private void filterOrdersByStatus(int tabPosition) {
        List<Order> filteredOrders = new ArrayList<>();

        switch (tabPosition) {
            case 0: // Tất cả
                currentFilter = null;
                filteredOrders.addAll(allOrders);
                break;
            case 1: // Chờ xác nhận
                currentFilter = OrderStatus.PENDING;
                break;
            case 2: // Đang giao
                currentFilter = OrderStatus.SHIPPING;
                break;
            case 3: // Đã giao
                currentFilter = OrderStatus.DELIVERED;
                break;
            case 4: // Đã hủy
                currentFilter = OrderStatus.CANCELLED;
                break;
        }

        if (currentFilter != null) {
            for (Order order : allOrders) {
                if (order.getOrderStatus() == currentFilter) {
                    filteredOrders.add(order);
                }
            }
        }

        updateOrdersList(filteredOrders);
    }

    private void loadOrders() {
        swipeRefreshLayout.setRefreshing(true);

        // Get current user ID
        UserManager userManager = UserManager.getInstance(this);
        String userId = userManager.getCurrentUserId();
        
        if (userId != null) {
            orderManager.getUserOrders(userId, new OrderListCallback() {
                @Override
                public void onSuccess(List<Order> orders) {
                    runOnUiThread(() -> {
                        allOrders = orders;
                        
                        // Apply current filter
                        int currentTab = tabLayoutOrderStatus.getSelectedTabPosition();
                        filterOrdersByStatus(currentTab);
                        
                        swipeRefreshLayout.setRefreshing(false);
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        // Show empty state instead of mock data
                        allOrders.clear();
                        updateOrdersList(allOrders);
                        
                        swipeRefreshLayout.setRefreshing(false);
                        Log.d("MyOrdersActivity", "No orders found for user: " + error);
                    });
                }
            });
        } else {
            // No user logged in, show empty state
            allOrders.clear();
            updateOrdersList(allOrders);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void updateOrdersList(List<Order> orders) {
        if (orders.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            ordersAdapter.updateOrders(orders);
        }
    }

    private void showEmptyState() {
        recyclerViewOrders.setVisibility(View.GONE);
        textEmptyState.setVisibility(View.VISIBLE);
        
        String message = "Không có đơn hàng nào";
        if (currentFilter != null) {
            message = "Không có đơn hàng " + getStatusDisplayName(currentFilter).toLowerCase();
        }
        textEmptyState.setText(message);
    }

    private void hideEmptyState() {
        recyclerViewOrders.setVisibility(View.VISIBLE);
        textEmptyState.setVisibility(View.GONE);
    }

    private String getStatusDisplayName(OrderStatus status) {
        switch (status) {
            case PENDING: return "chờ xác nhận";
            case CONFIRMED: return "đã xác nhận";
            case SHIPPING: return "đang giao";
            case DELIVERED: return "đã giao";
            case CANCELLED: return "đã hủy";
            default: return "";
        }
    }

    // Mock data for demo - DISABLED to show only real orders
    /*
    private List<Order> createMockOrders() {
        List<Order> orders = new ArrayList<>();
        
        // Create some mock orders with different statuses
        Order order1 = createMockOrder("ORD_001", OrderStatus.PENDING, 29990000);
        Order order2 = createMockOrder("ORD_002", OrderStatus.SHIPPING, 24990000);
        Order order3 = createMockOrder("ORD_003", OrderStatus.DELIVERED, 15990000);
        Order order4 = createMockOrder("ORD_004", OrderStatus.CANCELLED, 19990000);
        
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);
        orders.add(order4);
        
        return orders;
    }

    private Order createMockOrder(String orderId, OrderStatus status, double totalAmount) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setOrderStatus(status);
        
        // Mock pricing
        com.example.phoneshopapp.models.PricingInfo pricing = new com.example.phoneshopapp.models.PricingInfo();
        pricing.setTotal(totalAmount);
        order.setPricing(pricing);
        
        // Mock date
        order.setCreatedAt(new java.util.Date());
        
        return order;
    }
    */

    @Override
    public void onOrderClick(Order order) {
        // Navigate to order detail
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("order_id", order.getOrderId());
        startActivity(intent);
    }

    /**
     * Handle review button click
     * QUAN TRỌNG: Kiểm tra đơn hàng đã review chưa trước khi navigate
     */
    @Override
    public void onReviewClick(Order order) {
        // Kiểm tra đơn hàng đã review chưa
        reviewManager.checkCanReview(order.getOrderId(), new BooleanCallback() {
            @Override
            public void onResult(boolean canReview) {
                if (!canReview) {
                    // Đã review rồi
                    Toast.makeText(MyOrdersActivity.this,
                            "Đơn hàng này đã được đánh giá rồi",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Chưa review, cho phép đánh giá
                // Navigate đến màn hình đánh giá
                navigateToReviewScreen(order);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MyOrdersActivity.this,
                        "Lỗi: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Navigate to review screen
     * Handle case: 1 sản phẩm vs nhiều sản phẩm
     */
    private void navigateToReviewScreen(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            Toast.makeText(this, "Đơn hàng không có sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Nếu order có nhiều sản phẩm, show dialog chọn
        // Hiện tại: Chỉ support 1 sản phẩm, lấy item đầu tiên
        if (order.getItems().size() == 1) {
            com.example.phoneshopapp.models.OrderItem item = order.getItems().get(0);
            openReviewActivity(order.getOrderId(), item);
        } else {
            // TODO Phase 2: Show dialog để user chọn sản phẩm muốn đánh giá
            // Tạm thời: Lấy sản phẩm đầu tiên
            com.example.phoneshopapp.models.OrderItem item = order.getItems().get(0);
            openReviewActivity(order.getOrderId(), item);
            
            Toast.makeText(this, 
                    "Đơn hàng có nhiều sản phẩm. Đang đánh giá sản phẩm đầu tiên.", 
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Open ProductReviewActivity với thông tin đầy đủ
     */
    private void openReviewActivity(String orderId, com.example.phoneshopapp.models.OrderItem item) {
        Intent intent = new Intent(this, ProductReviewActivity.class);
        
        // Pass order info
        intent.putExtra("order_id", orderId);
        
        // Pass product info
        intent.putExtra("product_id", item.getProductId());
        intent.putExtra("product_name", item.getProductName());
        intent.putExtra("product_image", item.getImageUrl());
        
        // Pass variant info (QUAN TRỌNG)
        intent.putExtra("variant_id", item.getVariantId());
        intent.putExtra("variant_name", item.getVariantName());
        intent.putExtra("variant_color", item.getVariantColor());
        intent.putExtra("variant_ram", item.getVariantRam());
        intent.putExtra("variant_storage", item.getVariantStorage());
        
        startActivity(intent);
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