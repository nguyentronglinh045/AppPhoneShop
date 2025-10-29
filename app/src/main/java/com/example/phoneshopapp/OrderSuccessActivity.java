package com.example.phoneshopapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

public class OrderSuccessActivity extends AppCompatActivity {

    private MaterialTextView textOrderId;
    private MaterialTextView textTotalAmount;
    private MaterialTextView textEstimatedDelivery;
    private MaterialButton btnViewOrder;
    private MaterialButton btnContinueShopping;

    private String orderId;
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        initViews();
        getIntentData();
        setupClickListeners();
        displayOrderInfo();
    }

    private void initViews() {
        textOrderId = findViewById(R.id.textOrderId);
        textTotalAmount = findViewById(R.id.textTotalAmount);
        textEstimatedDelivery = findViewById(R.id.textEstimatedDelivery);
        btnViewOrder = findViewById(R.id.btnViewOrder);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            orderId = intent.getStringExtra("order_id");
            totalAmount = intent.getDoubleExtra("total_amount", 0);
        }
    }

    private void setupClickListeners() {
        btnViewOrder.setOnClickListener(v -> {
            // TODO: Navigate to MyOrdersActivity or OrderDetailActivity
            Intent intent = new Intent(this, MyOrdersActivity.class);
            startActivity(intent);
            finish();
        });

        btnContinueShopping.setOnClickListener(v -> {
            // Navigate back to home
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void displayOrderInfo() {
        if (orderId != null) {
            textOrderId.setText("Mã đơn hàng: " + orderId);
        }

        if (totalAmount > 0) {
            textTotalAmount.setText(String.format("Tổng tiền: ₫%.0f", totalAmount));
        }

        // Mock estimated delivery time (2-3 days from now)
        long currentTime = System.currentTimeMillis();
        long deliveryTime = currentTime + (2 * 24 * 60 * 60 * 1000); // 2 days
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        String deliveryDate = sdf.format(new java.util.Date(deliveryTime));
        textEstimatedDelivery.setText("Dự kiến giao hàng: " + deliveryDate);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Redirect to home instead of going back to checkout
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}