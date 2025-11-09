package com.example.phoneshopapp.managers;

import android.content.Context;
import android.util.Log;

import com.example.phoneshopapp.models.Order;
import com.example.phoneshopapp.models.OrderItem;
import com.example.phoneshopapp.models.OrderStatus;
import com.example.phoneshopapp.models.PaymentStatus;
import com.example.phoneshopapp.models.CartItem;
import com.example.phoneshopapp.models.CustomerInfo;
import com.example.phoneshopapp.models.PricingInfo;
import com.example.phoneshopapp.models.PaymentInfo;
import com.example.phoneshopapp.models.PaymentMethod;
import com.example.phoneshopapp.repositories.OrderRepository;
import com.example.phoneshopapp.repositories.FirebaseOrderRepository;
import com.example.phoneshopapp.repositories.callbacks.OrderCreationCallback;
import com.example.phoneshopapp.repositories.callbacks.OrderCallback;
import com.example.phoneshopapp.repositories.callbacks.OrdersCallback;
import com.example.phoneshopapp.repositories.callbacks.UpdateCallback;
import com.example.phoneshopapp.data.cart.CartManager;
import com.example.phoneshopapp.data.cart.CartRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Singleton class quản lý logic đơn hàng
 * Handles order creation, retrieval, updates and business logic
 */
public class OrderManager {
    private static final String TAG = "OrderManager";
    private static final double SHIPPING_FEE = 30000.0; // Fixed shipping fee: 30,000 VND
    private static final int ESTIMATED_DELIVERY_DAYS = 3; // 3 days delivery time

    private static OrderManager instance;
    private final OrderRepository orderRepository;
    private final Context context;

    private OrderManager(Context context) {
        this.context = context.getApplicationContext();
        this.orderRepository = new FirebaseOrderRepository();
    }

    public static synchronized OrderManager getInstance(Context context) {
        if (instance == null) {
            instance = new OrderManager(context);
        }
        return instance;
    }

    /**
     * Create order from cart items
     * @param cartItems List of cart items to create order from (pre-filtered)
     * @param customerInfo Customer information
     * @param paymentMethod Selected payment method
     * @param note Order note (optional)
     * @param callback Callback for success/error handling
     */
    public void createOrderFromCart(List<CartItem> cartItems, CustomerInfo customerInfo, 
                                   PaymentMethod paymentMethod, String note, OrderCreationCallback callback) {
        
        if (cartItems == null || cartItems.isEmpty()) {
            callback.onError("Vui lòng chọn sản phẩm để đặt hàng");
            return;
        }
        
        Log.d(TAG, "Creating order with " + cartItems.size() + " items");

        // Validate customer info
        if (!isValidCustomerInfo(customerInfo)) {
            callback.onError("Thông tin khách hàng không hợp lệ");
            return;
        }

        // Convert cart items to order items
        List<OrderItem> orderItems = convertCartItemsToOrderItems(cartItems);
        
        // Calculate pricing
        PricingInfo pricingInfo = calculatePricing(orderItems);
        
        // Create payment info
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setMethod(paymentMethod);
        paymentInfo.setStatus(PaymentStatus.PENDING);
        paymentInfo.setPaidAt(null);
        
        // Set customer note
        customerInfo.setNote(note != null ? note : "");
        
        // Create order
        Order order = new Order();
        order.setUserId(getUserId());
        order.setCustomerInfo(customerInfo);
        order.setItems(orderItems);
        order.setPricing(pricingInfo);
        order.setPaymentInfo(paymentInfo);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setEstimatedDelivery(calculateEstimatedDelivery());
        
        // Create order in repository
        orderRepository.createOrder(order, new OrderCreationCallback() {
            @Override
            public void onSuccess(Order createdOrder) {
                Log.d(TAG, "Order created successfully: " + createdOrder.getOrderId());
                
                // Clear the items that were used for this order
                CartManager cartManager = CartManager.getInstance();
                List<String> itemIdsToRemove = new ArrayList<>();
                for (CartItem item : cartItems) {
                    if (item.getId() != null) {
                        itemIdsToRemove.add(item.getId());
                    }
                }
                
                if (!itemIdsToRemove.isEmpty()) {
                    cartManager.getCartRepository().deleteMultipleItems(itemIdsToRemove, new CartRepository.OnCartOperationListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Cart items cleared after order creation: " + itemIdsToRemove.size() + " items");
                            cartManager.refreshCart(); // Refresh to update UI
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.w(TAG, "Failed to clear cart items: " + e.getMessage());
                        }
                    });
                }
                
                callback.onSuccess(createdOrder);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to create order: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Get all orders for current user
     * @param callback Callback for success/error handling
     */
    public void getUserOrders(OrdersCallback callback) {
        String userId = getUserId();
        if (userId == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }
        
        orderRepository.getUserOrders(userId, callback);
    }

    /**
     * Get user orders by user ID
     * @param userId User ID to get orders for
     * @param callback Callback for success/error handling
     */
    public void getUserOrders(String userId, com.example.phoneshopapp.repositories.callbacks.OrderListCallback callback) {
        if (userId == null) {
            callback.onError("User ID không hợp lệ");
            return;
        }
        
        // Convert OrdersCallback to OrderListCallback
        orderRepository.getUserOrders(userId, new OrdersCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                callback.onSuccess(orders);
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Get order details by order ID
     * @param orderId Order ID to retrieve
     * @param callback Callback for success/error handling
     */
    public void getOrderDetails(String orderId, OrderCallback callback) {
        if (orderId == null || orderId.trim().isEmpty()) {
            callback.onError("Mã đơn hàng không hợp lệ");
            return;
        }
        
        orderRepository.getOrderById(orderId, callback);
    }

    /**
     * Update order status
     * @param orderId Order ID to update
     * @param status New order status
     * @param callback Callback for success/error handling
     */
    public void updateOrderStatus(String orderId, OrderStatus status, UpdateCallback callback) {
        if (orderId == null || orderId.trim().isEmpty()) {
            callback.onError("Mã đơn hàng không hợp lệ");
            return;
        }
        
        orderRepository.updateOrderStatus(orderId, status, callback);
    }

    /**
     * Cancel an order (only if status is PENDING or CONFIRMED)
     * @param orderId Order ID to cancel
     * @param callback Callback for success/error handling
     */
    public void cancelOrder(String orderId, UpdateCallback callback) {
        if (orderId == null || orderId.trim().isEmpty()) {
            callback.onError("Mã đơn hàng không hợp lệ");
            return;
        }
        
        // First check if order can be cancelled
        orderRepository.getOrderById(orderId, new OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                if (canCancelOrder(order)) {
                    orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED, callback);
                } else {
                    callback.onError("Không thể hủy đơn hàng ở trạng thái hiện tại");
                }
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Reorder - create new order from existing order
     * @param originalOrder Original order to reorder
     * @param callback Callback for success/error handling
     */
    public void reorder(Order originalOrder, OrderCreationCallback callback) {
        if (originalOrder == null) {
            callback.onError("Đơn hàng không hợp lệ");
            return;
        }

        // Create new order with same items but new timestamps
        Order newOrder = new Order();
        newOrder.setUserId(getUserId());
        newOrder.setCustomerInfo(originalOrder.getCustomerInfo());
        newOrder.setItems(originalOrder.getItems());
        
        // Recalculate pricing (prices might have changed)
        PricingInfo newPricing = calculatePricing(originalOrder.getItems());
        newOrder.setPricing(newPricing);
        
        // Set new payment info
        PaymentInfo newPaymentInfo = new PaymentInfo();
        newPaymentInfo.setMethod(originalOrder.getPaymentInfo().getMethod());
        newPaymentInfo.setStatus(PaymentStatus.PENDING);
        newOrder.setPaymentInfo(newPaymentInfo);
        
        newOrder.setOrderStatus(OrderStatus.PENDING);
        newOrder.setEstimatedDelivery(calculateEstimatedDelivery());
        
        orderRepository.createOrder(newOrder, callback);
    }

    // Helper methods

    private boolean isValidCustomerInfo(CustomerInfo customerInfo) {
        return customerInfo != null &&
               customerInfo.getFullName() != null && !customerInfo.getFullName().trim().isEmpty() &&
               customerInfo.getPhone() != null && !customerInfo.getPhone().trim().isEmpty() &&
               customerInfo.getEmail() != null && !customerInfo.getEmail().trim().isEmpty() &&
               customerInfo.getAddress() != null && !customerInfo.getAddress().trim().isEmpty();
    }

    private List<OrderItem> convertCartItemsToOrderItems(List<CartItem> cartItems) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.fromCartItem(cartItem);
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    private PricingInfo calculatePricing(List<OrderItem> orderItems) {
        double subtotal = 0.0;
        for (OrderItem item : orderItems) {
            subtotal += item.getTotalPrice();
        }
        
        double discount = 0.0; // No discount for now
        double total = subtotal + SHIPPING_FEE - discount;
        
        PricingInfo pricingInfo = new PricingInfo();
        pricingInfo.setSubtotal(subtotal);
        pricingInfo.setShippingFee(SHIPPING_FEE);
        pricingInfo.setDiscount(discount);
        pricingInfo.setTotal(total);
        
        return pricingInfo;
    }

    private Date calculateEstimatedDelivery() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, ESTIMATED_DELIVERY_DAYS);
        return calendar.getTime();
    }

    private boolean canCancelOrder(Order order) {
        OrderStatus status = order.getOrderStatus();
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    private String getUserId() {
        // Get current user ID from UserManager
        com.example.phoneshopapp.UserManager userManager = 
                com.example.phoneshopapp.UserManager.getInstance(context);
        return userManager.getCurrentUserId();
    }

    public double getShippingFee() {
        return SHIPPING_FEE;
    }

    public int getEstimatedDeliveryDays() {
        return ESTIMATED_DELIVERY_DAYS;
    }
}