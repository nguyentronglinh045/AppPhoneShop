package com.example.phoneshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phoneshopapp.adapters.CheckoutItemsAdapter;
import com.example.phoneshopapp.data.cart.CartManager;
import com.example.phoneshopapp.dialogs.AddAddressDialog;
import com.example.phoneshopapp.managers.AddressManager;
import com.example.phoneshopapp.managers.OrderManager;
import com.example.phoneshopapp.managers.PaymentManager;
import com.example.phoneshopapp.models.*;
import com.example.phoneshopapp.repositories.callbacks.OrderCreationCallback;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private static final int REQUEST_ADD_ADDRESS = 1001;

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView recyclerViewCheckoutItems;
    private MaterialTextView textTotalItems, textSubtotal, textShippingFee, textTotal, textDiscount;
    private LinearLayout layoutDiscount;
    private TextInputEditText editTextFullName, editTextPhone, editTextEmail, editTextNote, editTextDiscountCode;
    private Spinner spinnerAddress;
    private MaterialButton btnAddAddress, btnPlaceOrder, btnBack, btnApplyDiscount;
    private RadioGroup radioGroupPayment;

    // Data
    private CheckoutItemsAdapter checkoutAdapter;
    private CartManager cartManager;
    private OrderManager orderManager;
    private PaymentManager paymentManager;
    private List<CartItem> cartItems;
    private List<Address> userAddresses;
    private Address selectedAddress;
    private PaymentMethod selectedPaymentMethod = PaymentMethod.COD;

    // Mock data for demo
    private double shippingFee = 30000; // Fixed shipping fee
    private double discountAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupPaymentMethods();
        setupClickListeners();

        // Initialize managers
        cartManager = CartManager.getInstance();
        cartManager.initialize(this);
        orderManager = OrderManager.getInstance(this);
        paymentManager = PaymentManager.getInstance(this);

        // Check if user is logged in
        UserManager userManager = UserManager.getInstance(this);
        if (!userManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadUserInfo();
        loadCartData();
        loadUserAddresses(); // Mock for now
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewCheckoutItems = findViewById(R.id.recyclerViewCheckoutItems);
        textTotalItems = findViewById(R.id.textTotalItems);
        textSubtotal = findViewById(R.id.textSubtotal);
        textShippingFee = findViewById(R.id.textShippingFee);
        textTotal = findViewById(R.id.textTotal);
        layoutDiscount = findViewById(R.id.layoutDiscount);
        textDiscount = findViewById(R.id.textDiscount);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextNote = findViewById(R.id.editTextNote);
        spinnerAddress = findViewById(R.id.spinnerAddress);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnBack = findViewById(R.id.btnBack);
        radioGroupPayment = findViewById(R.id.radioGroupPayment);
        editTextDiscountCode = findViewById(R.id.editTextDiscountCode);
        btnApplyDiscount = findViewById(R.id.btnApplyDiscount);
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
        checkoutAdapter = new CheckoutItemsAdapter();
        recyclerViewCheckoutItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCheckoutItems.setAdapter(checkoutAdapter);
    }

    private void setupPaymentMethods() {
        radioGroupPayment.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioCOD) {
                selectedPaymentMethod = PaymentMethod.COD;
            } else if (checkedId == R.id.radioBankTransfer) {
                selectedPaymentMethod = PaymentMethod.BANK_TRANSFER;
            } else if (checkedId == R.id.radioEWallet) {
                selectedPaymentMethod = PaymentMethod.EWALLET;
            }
        });
    }

    private void setupClickListeners() {
        btnAddAddress.setOnClickListener(v -> {
            android.util.Log.d("CheckoutActivity", "Add Address button clicked");
            showAddAddressDialog();
        });

        btnBack.setOnClickListener(v -> onBackPressed());

        btnPlaceOrder.setOnClickListener(v -> processOrder());

        btnApplyDiscount.setOnClickListener(v -> {
            String code = editTextDiscountCode.getText().toString().trim();
            if (code.equals("GIAM10")) {
                // Apply 10% discount
                double subtotal = 0;
                for (CartItem item : cartItems) {
                    subtotal += item.getProductPriceValue() * item.getQuantity();
                }
                discountAmount = subtotal * 0.1;
                updatePricingSummary();
                Toast.makeText(CheckoutActivity.this, "Mã giảm giá đã được áp dụng!", Toast.LENGTH_SHORT).show();
            } else {
                discountAmount = 0;
                updatePricingSummary();
                Toast.makeText(CheckoutActivity.this, "Mã giảm giá không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserInfo() {
        UserManager userManager = UserManager.getInstance(this);
        if (userManager.isLoggedIn()) {
            // Auto-fill user information
            editTextEmail.setText(userManager.getUserEmail());
            editTextFullName.setText(userManager.getDisplayName());
            // Phone will be filled from selected address
        }
    }

    private void loadCartData() {
        cartManager = CartManager.getInstance();
        cartManager.initialize(this);

        // Get selected item IDs from Intent
        Intent intent = getIntent();
        ArrayList<String> selectedItemIds = null;
        if (intent != null) {
            selectedItemIds = intent.getStringArrayListExtra("SELECTED_ITEM_IDS");
        }

        if (selectedItemIds == null || selectedItemIds.isEmpty()) {
            android.util.Log.w("CheckoutActivity", "No selected items in Intent, fallback to getSelectedItems()");
            // Fallback: try to get selected items from CartManager (may not work if session lost)
            cartItems = cartManager.getSelectedItems();
        } else {
            android.util.Log.d("CheckoutActivity", "Loading " + selectedItemIds.size() + " items from Intent");
            // Filter cart items by IDs passed from CartActivity
            List<CartItem> allCartItems = cartManager.getCartItems();
            cartItems = new ArrayList<>();
            for (CartItem item : allCartItems) {
                if (item.getId() != null && selectedItemIds.contains(item.getId())) {
                    cartItems.add(item);
                    android.util.Log.d("CheckoutActivity", "  Added item: " + item.getProductName() + " (id=" + item.getId() + ")");
                }
            }
        }

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
            android.util.Log.e("CheckoutActivity", "No cart items to checkout!");
            finish();
            return;
        }

        android.util.Log.d("CheckoutActivity", "✅ Loaded " + cartItems.size() + " items for checkout");
        checkoutAdapter.updateItems(convertCartItemsToOrderItems(cartItems));
        updatePricingSummary();
    }

    private void loadUserAddresses() {
        AddressManager addressManager = AddressManager.getInstance(this);
        addressManager.getUserAddresses(new com.example.phoneshopapp.repositories.callbacks.AddressesCallback() {
            @Override
            public void onSuccess(java.util.List<com.example.phoneshopapp.models.Address> addresses) {
                userAddresses = addresses;
                setupAddressSpinner();
            }

            @Override
            public void onError(String error) {
                // If no addresses or error, show empty list
                userAddresses = new java.util.ArrayList<>();
                setupAddressSpinner();
                android.util.Log.e("CheckoutActivity", "Error loading addresses: " + error);
            }
        });
    }

    private void setupAddressSpinner() {
        List<String> addressNames = new ArrayList<>();

        if (userAddresses == null || userAddresses.isEmpty()) {
            addressNames.add("Chưa có địa chỉ - Vui lòng thêm địa chỉ mới");
        } else {
            for (Address address : userAddresses) {
                String displayText = address.getAddressName() + " - " + address.getRecipientName();
                if (address.getFullAddress() != null && !address.getFullAddress().isEmpty()) {
                    displayText += " - " + address.getFullAddress();
                }
                addressNames.add(displayText);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, addressNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAddress.setAdapter(adapter);

        // Add spinner selection listener
        spinnerAddress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < userAddresses.size()) {
                    selectedAddress = userAddresses.get(position);
                    // Auto-fill phone number from selected address
                    if (selectedAddress.getPhone() != null && !selectedAddress.getPhone().isEmpty()) {
                        editTextPhone.setText(selectedAddress.getPhone());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Set default address if available
        for (int i = 0; i < userAddresses.size(); i++) {
            if (userAddresses.get(i).isDefault()) {
                spinnerAddress.setSelection(i);
                selectedAddress = userAddresses.get(i);
                if (selectedAddress.getPhone() != null && !selectedAddress.getPhone().isEmpty()) {
                    editTextPhone.setText(selectedAddress.getPhone());
                }
                break;
            }
        }
    }

    private void updatePricingSummary() {
        if (cartItems == null || cartItems.isEmpty()) {
            return;
        }

        int totalItems = 0;
        double subtotal = 0;

        for (CartItem item : cartItems) {
            totalItems += item.getQuantity();
            subtotal += item.getProductPriceValue() * item.getQuantity();
        }

        double total = subtotal + shippingFee - discountAmount;

        textTotalItems.setText(String.format("%d sản phẩm", totalItems));
        textSubtotal.setText(String.format("₫%.0f", subtotal));
        textShippingFee.setText(String.format("₫%.0f", shippingFee));
        textTotal.setText(String.format("₫%.0f", total));

        if (discountAmount > 0) {
            layoutDiscount.setVisibility(View.VISIBLE);
            textDiscount.setText(String.format("-₫%.0f", discountAmount));
        } else {
            layoutDiscount.setVisibility(View.GONE);
        }
    }

    private void processOrder() {
        if (!validateOrderInfo()) {
            return;
        }

        // Show loading
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Đang xử lý...");

        // Create customer info from form
        CustomerInfo customerInfo = createCustomerInfoFromForm();
        String note = editTextNote.getText() != null ? editTextNote.getText().toString().trim() : "";

        // Create order using OrderManager - pass cartItems explicitly
        orderManager.createOrderFromCart(cartItems, customerInfo, selectedPaymentMethod, note, new OrderCreationCallback() {
            @Override
            public void onSuccess(Order order) {
                runOnUiThread(() -> {
                    // Process payment if needed
                    if (paymentManager.requiresImmediateProcessing(selectedPaymentMethod)) {
                        processPayment(order);
                    } else {
                        // COD or other methods that don't require immediate processing
                        navigateToOrderSuccess(order);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    btnPlaceOrder.setEnabled(true);
                    btnPlaceOrder.setText("Đặt Hàng");
                    Toast.makeText(CheckoutActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private CustomerInfo createCustomerInfoFromForm() {
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setFullName(editTextFullName.getText().toString().trim());
        customerInfo.setPhone(editTextPhone.getText().toString().trim());
        customerInfo.setEmail(editTextEmail.getText().toString().trim());
        customerInfo.setAddress(selectedAddress.getFullAddress());

        return customerInfo;
    }

    private void processPayment(Order order) {
        paymentManager.processPayment(order, new com.example.phoneshopapp.repositories.callbacks.PaymentCallback() {
            @Override
            public void onSuccess(com.example.phoneshopapp.models.PaymentInfo paymentInfo) {
                runOnUiThread(() -> {
                    navigateToOrderSuccess(order);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    btnPlaceOrder.setEnabled(true);
                    btnPlaceOrder.setText("Đặt Hàng");
                    Toast.makeText(CheckoutActivity.this,
                            "Lỗi thanh toán: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void navigateToOrderSuccess(Order order) {
        Intent intent = new Intent(this, OrderSuccessActivity.class);
        intent.putExtra("order_id", order.getOrderId());
        intent.putExtra("total_amount", order.getPricing().getTotal());
        intent.putExtra("estimated_delivery", order.getEstimatedDelivery().getTime());
        startActivity(intent);
        finish();
    }

    private boolean validateOrderInfo() {
        // Validate customer info
        if (editTextFullName.getText() == null || editTextFullName.getText().toString().trim().isEmpty()) {
            editTextFullName.setError("Vui lòng nhập họ tên");
            return false;
        }

        if (editTextPhone.getText() == null || editTextPhone.getText().toString().trim().isEmpty()) {
            editTextPhone.setError("Vui lòng nhập số điện thoại");
            return false;
        }

        if (editTextEmail.getText() == null || editTextEmail.getText().toString().trim().isEmpty()) {
            editTextEmail.setError("Vui lòng nhập email");
            return false;
        }

        // Validate address
        if (selectedAddress == null) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate cart - must have selected items
        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào được chọn", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private Order createOrderFromForm() {
        // Create customer info
        CustomerInfo customerInfo = new CustomerInfo(
                editTextFullName.getText().toString().trim(),
                editTextPhone.getText().toString().trim(),
                editTextEmail.getText().toString().trim(),
                selectedAddress.getFullAddress(),
                editTextNote.getText() != null ? editTextNote.getText().toString().trim() : "");

        // Create order items
        List<OrderItem> orderItems = convertCartItemsToOrderItems(cartItems);

        // Create pricing info
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getProductPriceValue() * item.getQuantity();
        }
        PricingInfo pricingInfo = new PricingInfo(subtotal, shippingFee, discountAmount);

        // Create payment info
        PaymentInfo paymentInfo = new PaymentInfo(selectedPaymentMethod);

        // Create order
        UserManager userManager = UserManager.getInstance(this);
        String orderId = generateOrderId();

        return new Order(orderId, userManager.getCurrentUserId(), customerInfo,
                orderItems, pricingInfo, paymentInfo);
    }

    private void simulateOrderCreation(Order order) {
        // Show loading
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Đang xử lý...");

        // Simulate network delay
        new android.os.Handler().postDelayed(() -> {
            // Simulate success
            Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();

            // Navigate to success screen
            Intent intent = new Intent(this, OrderSuccessActivity.class);
            intent.putExtra("order_id", order.getOrderId());
            intent.putExtra("total_amount", order.getTotalAmount());
            startActivity(intent);

            // Clear cart and finish
            clearCartAndFinish();

        }, 2000);
    }

    private void clearCartAndFinish() {
        // TODO: Clear cart in real implementation
        finish();
    }

    private String generateOrderId() {
        // Simple order ID generation for demo
        return "ORD_" + System.currentTimeMillis();
    }

    private List<OrderItem> convertCartItemsToOrderItems(List<CartItem> cartItems) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            orderItems.add(OrderItem.fromCartItem(cartItem));
        }
        return orderItems;
    }

    // Mock data methods for demo
    private List<CartItem> createMockCartItems() {
        List<CartItem> items = new ArrayList<>();

        // Create mock CartItems using the correct constructor
        CartItem item1 = new CartItem();
        item1.setProductId("1");
        item1.setProductName("iPhone 15 Pro Max 256GB");
        item1.setProductPriceValue(29990000);
        item1.setQuantity(1);
        item1.setProductImageUrl("https://example.com/iphone.jpg");
        items.add(item1);

        CartItem item2 = new CartItem();
        item2.setProductId("2");
        item2.setProductName("Samsung Galaxy S24 Ultra");
        item2.setProductPriceValue(24990000);
        item2.setQuantity(1);
        item2.setProductImageUrl("https://example.com/samsung.jpg");
        items.add(item2);

        return items;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_ADDRESS && resultCode == RESULT_OK) {
            // Refresh address list
            loadUserAddresses();
            Toast.makeText(this, "Địa chỉ đã được cập nhật", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void showAddAddressDialog() {
        AddAddressDialog dialog = new AddAddressDialog(this, new AddAddressDialog.OnAddressAddedListener() {
            @Override
            public void onAddressAdded() {
                // Refresh address list when new address is added
                loadUserAddresses();
            }

            @Override
            public void onAddressCancelled() {
                // Do nothing when cancelled
            }
        });

        dialog.show();
    }
}