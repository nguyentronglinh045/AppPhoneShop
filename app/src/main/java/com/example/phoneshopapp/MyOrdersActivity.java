package com.example.phoneshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.phoneshopapp.adapters.OrdersAdapter;
import com.example.phoneshopapp.managers.OrderManager;
import com.example.phoneshopapp.models.Order;
import com.example.phoneshopapp.models.OrderStatus;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        // Initialize managers
        orderManager = OrderManager.getInstance(this);

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