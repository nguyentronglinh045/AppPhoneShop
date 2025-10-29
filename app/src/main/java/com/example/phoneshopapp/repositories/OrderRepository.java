package com.example.phoneshopapp.repositories;

import com.example.phoneshopapp.models.Order;
import com.example.phoneshopapp.models.OrderStatus;
import com.example.phoneshopapp.models.PaymentStatus;
import com.example.phoneshopapp.repositories.callbacks.OrderCreationCallback;
import com.example.phoneshopapp.repositories.callbacks.OrderCallback;
import com.example.phoneshopapp.repositories.callbacks.OrdersCallback;
import com.example.phoneshopapp.repositories.callbacks.UpdateCallback;

public interface OrderRepository {

    /**
     * Create a new order in Firestore
     * 
     * @param order    Order object to create
     * @param callback Callback for success/error handling
     */
    void createOrder(Order order, OrderCreationCallback callback);

    /**
     * Get all orders for a specific user
     * 
     * @param userId   User ID to get orders for
     * @param callback Callback for success/error handling
     */
    void getUserOrders(String userId, OrdersCallback callback);

    /**
     * Get order by order ID
     * 
     * @param orderId  Order ID to retrieve
     * @param callback Callback for success/error handling
     */
    void getOrderById(String orderId, OrderCallback callback);

    /**
     * Update order status
     * 
     * @param orderId  Order ID to update
     * @param status   New order status
     * @param callback Callback for success/error handling
     */
    void updateOrderStatus(String orderId, OrderStatus status, UpdateCallback callback);

    /**
     * Update payment status
     * 
     * @param orderId  Order ID to update
     * @param status   New payment status
     * @param callback Callback for success/error handling
     */
    void updatePaymentStatus(String orderId, PaymentStatus status, UpdateCallback callback);

    /**
     * Generate unique order ID
     * 
     * @return Unique order ID string
     */
    String generateOrderId();

    /**
     * Get all orders (for admin)
     * 
     * @param callback Callback for success/error handling
     */
    void getAllOrders(OrdersCallback callback);
}