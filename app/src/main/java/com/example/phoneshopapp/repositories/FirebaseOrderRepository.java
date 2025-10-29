package com.example.phoneshopapp.repositories;

import android.util.Log;
import com.example.phoneshopapp.models.Order;
import com.example.phoneshopapp.models.OrderStatus;
import com.example.phoneshopapp.models.PaymentStatus;
import com.example.phoneshopapp.models.StatusHistory;
import com.example.phoneshopapp.repositories.callbacks.OrderCreationCallback;
import com.example.phoneshopapp.repositories.callbacks.OrderCallback;
import com.example.phoneshopapp.repositories.callbacks.OrdersCallback;
import com.example.phoneshopapp.repositories.callbacks.UpdateCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FirebaseOrderRepository implements OrderRepository {
    private static final String TAG = "FirebaseOrderRepository";
    private static final String COLLECTION_ORDERS = "orders";
    private static final String COLLECTION_ORDER_SEQUENCE = "order_sequence";
    private static final String SEQUENCE_DOC_ID = "counter";

    private final FirebaseFirestore db;
    private final CollectionReference ordersRef;
    private final CollectionReference sequenceRef;

    public FirebaseOrderRepository() {
        db = FirebaseFirestore.getInstance();
        ordersRef = db.collection(COLLECTION_ORDERS);
        sequenceRef = db.collection(COLLECTION_ORDER_SEQUENCE);
    }

    @Override
    public void createOrder(Order order, OrderCreationCallback callback) {
        // Generate order ID if not set
        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
            order.setOrderId(generateOrderId());
        }

        // Set creation timestamp
        Date now = new Date();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        // Add initial status history
        List<StatusHistory> statusHistory = new ArrayList<>();
        statusHistory.add(new StatusHistory(
                order.getOrderStatus(),
                now,
                "Đơn hàng được tạo"));
        order.setStatusHistory(statusHistory);

        // Convert order to Map for Firestore
        Map<String, Object> orderData = orderToMap(order);

        ordersRef.document(order.getOrderId())
                .set(orderData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Order created successfully: " + order.getOrderId());
                    callback.onSuccess(order);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating order", e);
                    callback.onError("Lỗi tạo đơn hàng: " + e.getMessage());
                });
    }

    @Override
    public void getUserOrders(String userId, OrdersCallback callback) {
        ordersRef.whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orders = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Order order = documentToOrder(document);
                            orders.add(order);
                        } catch (Exception e) {
                            Log.e(TAG, "Error converting document to Order: " + document.getId(), e);
                        }
                    }

                    // Sort by createdAt descending (newest first)
                    orders.sort((o1, o2) -> {
                        if (o1.getCreatedAt() == null && o2.getCreatedAt() == null)
                            return 0;
                        if (o1.getCreatedAt() == null)
                            return 1;
                        if (o2.getCreatedAt() == null)
                            return -1;
                        return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                    });

                    Log.d(TAG, "Retrieved " + orders.size() + " orders for user: " + userId);
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user orders", e);
                    callback.onError("Lỗi tải danh sách đơn hàng: " + e.getMessage());
                });
    }

    // Helper method to convert Firestore document to Order object
    private Order documentToOrder(com.google.firebase.firestore.DocumentSnapshot document) {
        Order order = new Order();

        // Set basic fields
        order.setOrderId(document.getString("orderId"));
        order.setUserId(document.getString("userId"));

        // Convert orderStatus from String to enum
        String statusStr = document.getString("orderStatus");
        if (statusStr != null) {
            try {
                order.setOrderStatus(OrderStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Invalid order status: " + statusStr);
                order.setOrderStatus(OrderStatus.PENDING);
            }
        }

        // Convert dates
        order.setCreatedAt(document.getDate("createdAt"));
        order.setUpdatedAt(document.getDate("updatedAt"));
        order.setEstimatedDelivery(document.getDate("estimatedDelivery"));

        // Convert nested objects
        Map<String, Object> customerInfoMap = (Map<String, Object>) document.get("customerInfo");
        if (customerInfoMap != null) {
            order.setCustomerInfo(mapToCustomerInfo(customerInfoMap));
        }

        List<Map<String, Object>> itemsList = (List<Map<String, Object>>) document.get("items");
        if (itemsList != null) {
            order.setItems(mapListToOrderItems(itemsList));
        }

        Map<String, Object> pricingMap = (Map<String, Object>) document.get("pricing");
        if (pricingMap != null) {
            order.setPricing(mapToPricingInfo(pricingMap));
        }

        Map<String, Object> paymentMap = (Map<String, Object>) document.get("paymentInfo");
        if (paymentMap != null) {
            order.setPaymentInfo(mapToPaymentInfo(paymentMap));
        }

        List<Map<String, Object>> statusHistoryList = (List<Map<String, Object>>) document.get("statusHistory");
        if (statusHistoryList != null) {
            order.setStatusHistory(mapListToStatusHistory(statusHistoryList));
        }

        return order;
    }

    @Override
    public void getOrderById(String orderId, OrderCallback callback) {
        ordersRef.document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            Order order = documentToOrder(documentSnapshot);
                            Log.d(TAG, "Order retrieved: " + orderId);
                            callback.onSuccess(order);
                        } catch (Exception e) {
                            Log.e(TAG, "Error converting order", e);
                            callback.onError("Lỗi xử lý thông tin đơn hàng");
                        }
                    } else {
                        Log.w(TAG, "Order not found: " + orderId);
                        callback.onError("Không tìm thấy đơn hàng");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting order", e);
                    callback.onError("Lỗi tải thông tin đơn hàng: " + e.getMessage());
                });
    }

    @Override
    public void updateOrderStatus(String orderId, OrderStatus status, UpdateCallback callback) {
        DocumentReference orderRef = ordersRef.document(orderId);

        // First get the current order to update status history
        orderRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Order order = documentSnapshot.toObject(Order.class);
                        if (order != null) {
                            // Update order status
                            order.setOrderStatus(status);
                            order.setUpdatedAt(new Date());

                            // Add to status history
                            List<StatusHistory> statusHistory = order.getStatusHistory();
                            if (statusHistory == null) {
                                statusHistory = new ArrayList<>();
                            }
                            statusHistory.add(new StatusHistory(
                                    status,
                                    new Date(),
                                    "Cập nhật trạng thái: " + status.getDisplayName()));
                            order.setStatusHistory(statusHistory);

                            // Update in Firestore
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("orderStatus", status.name());
                            updates.put("updatedAt", new Date());
                            updates.put("statusHistory", statusHistoryToMapList(statusHistory));

                            orderRef.update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Order status updated: " + orderId);
                                        callback.onSuccess();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error updating order status", e);
                                        callback.onError("Lỗi cập nhật trạng thái: " + e.getMessage());
                                    });
                        }
                    } else {
                        callback.onError("Không tìm thấy đơn hàng");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting order for status update", e);
                    callback.onError("Lỗi cập nhật trạng thái: " + e.getMessage());
                });
    }

    @Override
    public void updatePaymentStatus(String orderId, PaymentStatus status, UpdateCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("paymentInfo.status", status.name());
        updates.put("updatedAt", new Date());

        if (status == PaymentStatus.PAID) {
            updates.put("paymentInfo.paidAt", new Date());
        }

        ordersRef.document(orderId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Payment status updated: " + orderId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating payment status", e);
                    callback.onError("Lỗi cập nhật trạng thái thanh toán: " + e.getMessage());
                });
    }

    @Override
    public String generateOrderId() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String datePart = dateFormat.format(new Date());
        long timestamp = System.currentTimeMillis();
        return "ORD_" + datePart + "_" + String.valueOf(timestamp).substring(8);
    }

    @Override
    public void getAllOrders(OrdersCallback callback) {
        ordersRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orders = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Order order = documentToOrder(document);
                            orders.add(order);
                        } catch (Exception e) {
                            Log.e(TAG, "Error converting document to Order: " + document.getId(), e);
                        }
                    }

                    // Sort by createdAt descending (newest first)
                    orders.sort((o1, o2) -> {
                        if (o1.getCreatedAt() == null && o2.getCreatedAt() == null)
                            return 0;
                        if (o1.getCreatedAt() == null)
                            return 1;
                        if (o2.getCreatedAt() == null)
                            return -1;
                        return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                    });

                    Log.d(TAG, "Retrieved " + orders.size() + " orders (all)");
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting all orders", e);
                    callback.onError("Error loading orders: " + e.getMessage());
                });
    }

    // Helper method to convert Order to Map for Firestore
    private Map<String, Object> orderToMap(Order order) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", order.getOrderId());
        map.put("userId", order.getUserId());
        map.put("customerInfo", customerInfoToMap(order.getCustomerInfo()));
        map.put("items", orderItemsToMapList(order.getItems()));
        map.put("pricing", pricingInfoToMap(order.getPricing()));
        map.put("paymentInfo", paymentInfoToMap(order.getPaymentInfo()));
        map.put("orderStatus", order.getOrderStatus().name());
        map.put("statusHistory", statusHistoryToMapList(order.getStatusHistory()));
        map.put("createdAt", order.getCreatedAt());
        map.put("updatedAt", order.getUpdatedAt());
        map.put("estimatedDelivery", order.getEstimatedDelivery());
        return map;
    }

    // Helper methods for object to map conversions
    private Map<String, Object> customerInfoToMap(com.example.phoneshopapp.models.CustomerInfo customerInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("fullName", customerInfo.getFullName());
        map.put("phone", customerInfo.getPhone());
        map.put("email", customerInfo.getEmail());
        map.put("address", customerInfo.getAddress());
        map.put("note", customerInfo.getNote());
        return map;
    }

    private List<Map<String, Object>> orderItemsToMapList(List<com.example.phoneshopapp.models.OrderItem> items) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (com.example.phoneshopapp.models.OrderItem item : items) {
            Map<String, Object> map = new HashMap<>();
            map.put("productId", item.getProductId());
            map.put("productName", item.getProductName());
            map.put("price", item.getPrice());
            map.put("quantity", item.getQuantity());
            map.put("imageUrl", item.getImageUrl());
            map.put("totalPrice", item.getTotalPrice());

            // Add variant fields
            if (item.getVariantId() != null) {
                map.put("variantId", item.getVariantId());
                map.put("variantName", item.getVariantName());
                map.put("variantShortName", item.getVariantShortName());
                map.put("variantColor", item.getVariantColor());
                map.put("variantColorHex", item.getVariantColorHex());
                map.put("variantRam", item.getVariantRam());
                map.put("variantStorage", item.getVariantStorage());
            }

            list.add(map);
        }
        return list;
    }

    private Map<String, Object> pricingInfoToMap(com.example.phoneshopapp.models.PricingInfo pricing) {
        Map<String, Object> map = new HashMap<>();
        map.put("subtotal", pricing.getSubtotal());
        map.put("shippingFee", pricing.getShippingFee());
        map.put("discount", pricing.getDiscount());
        map.put("total", pricing.getTotal());
        return map;
    }

    private Map<String, Object> paymentInfoToMap(com.example.phoneshopapp.models.PaymentInfo paymentInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("method", paymentInfo.getMethod().name());
        map.put("status", paymentInfo.getStatus().name());
        map.put("paidAt", paymentInfo.getPaidAt());
        map.put("transactionId", paymentInfo.getTransactionId());
        return map;
    }

    private List<Map<String, Object>> statusHistoryToMapList(List<StatusHistory> statusHistory) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (statusHistory != null) {
            for (StatusHistory history : statusHistory) {
                Map<String, Object> map = new HashMap<>();
                map.put("status", history.getStatus());
                map.put("timestamp", history.getTimestamp());
                map.put("note", history.getNote());
                list.add(map);
            }
        }
        return list;
    }

    // Reverse conversion methods: Map to Object
    private com.example.phoneshopapp.models.CustomerInfo mapToCustomerInfo(Map<String, Object> map) {
        com.example.phoneshopapp.models.CustomerInfo info = new com.example.phoneshopapp.models.CustomerInfo();
        info.setFullName((String) map.get("fullName"));
        info.setPhone((String) map.get("phone"));
        info.setEmail((String) map.get("email"));
        info.setAddress((String) map.get("address"));
        info.setNote((String) map.get("note"));
        return info;
    }

    private List<com.example.phoneshopapp.models.OrderItem> mapListToOrderItems(List<Map<String, Object>> list) {
        List<com.example.phoneshopapp.models.OrderItem> items = new ArrayList<>();
        for (Map<String, Object> map : list) {
            com.example.phoneshopapp.models.OrderItem item = new com.example.phoneshopapp.models.OrderItem();
            item.setProductId((String) map.get("productId"));
            item.setProductName((String) map.get("productName"));

            // Handle price (could be Double or Long)
            Object priceObj = map.get("price");
            if (priceObj instanceof Number) {
                item.setPrice(((Number) priceObj).doubleValue());
            }

            Object quantityObj = map.get("quantity");
            if (quantityObj instanceof Number) {
                item.setQuantity(((Number) quantityObj).intValue());
            }

            item.setImageUrl((String) map.get("imageUrl"));

            Object totalPriceObj = map.get("totalPrice");
            if (totalPriceObj instanceof Number) {
                item.setTotalPrice(((Number) totalPriceObj).doubleValue());
            } else {
                item.calculateTotalPrice();
            }

            // Handle variant fields
            item.setVariantId((String) map.get("variantId"));
            item.setVariantName((String) map.get("variantName"));
            item.setVariantShortName((String) map.get("variantShortName"));
            item.setVariantColor((String) map.get("variantColor"));
            item.setVariantColorHex((String) map.get("variantColorHex"));
            item.setVariantRam((String) map.get("variantRam"));
            item.setVariantStorage((String) map.get("variantStorage"));

            items.add(item);
        }
        return items;
    }

    private com.example.phoneshopapp.models.PricingInfo mapToPricingInfo(Map<String, Object> map) {
        com.example.phoneshopapp.models.PricingInfo pricing = new com.example.phoneshopapp.models.PricingInfo();

        Object subtotalObj = map.get("subtotal");
        if (subtotalObj instanceof Number) {
            pricing.setSubtotal(((Number) subtotalObj).doubleValue());
        }

        Object shippingFeeObj = map.get("shippingFee");
        if (shippingFeeObj instanceof Number) {
            pricing.setShippingFee(((Number) shippingFeeObj).doubleValue());
        }

        Object discountObj = map.get("discount");
        if (discountObj instanceof Number) {
            pricing.setDiscount(((Number) discountObj).doubleValue());
        }

        Object totalObj = map.get("total");
        if (totalObj instanceof Number) {
            pricing.setTotal(((Number) totalObj).doubleValue());
        }

        return pricing;
    }

    private com.example.phoneshopapp.models.PaymentInfo mapToPaymentInfo(Map<String, Object> map) {
        com.example.phoneshopapp.models.PaymentInfo payment = new com.example.phoneshopapp.models.PaymentInfo();

        String methodStr = (String) map.get("method");
        if (methodStr != null) {
            try {
                payment.setMethod(com.example.phoneshopapp.models.PaymentMethod.valueOf(methodStr));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Invalid payment method: " + methodStr);
            }
        }

        String statusStr = (String) map.get("status");
        if (statusStr != null) {
            try {
                payment.setStatus(com.example.phoneshopapp.models.PaymentStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Invalid payment status: " + statusStr);
            }
        }

        // Convert paidAt from Firebase Timestamp to Date
        Object paidAtObj = map.get("paidAt");
        if (paidAtObj instanceof com.google.firebase.Timestamp) {
            payment.setPaidAt(((com.google.firebase.Timestamp) paidAtObj).toDate());
        } else if (paidAtObj instanceof Date) {
            payment.setPaidAt((Date) paidAtObj);
        }

        payment.setTransactionId((String) map.get("transactionId"));

        return payment;
    }

    private List<StatusHistory> mapListToStatusHistory(List<Map<String, Object>> list) {
        List<StatusHistory> statusHistory = new ArrayList<>();
        for (Map<String, Object> map : list) {
            String statusStr = (String) map.get("status");
            OrderStatus status = OrderStatus.PENDING;
            if (statusStr != null) {
                try {
                    status = OrderStatus.valueOf(statusStr);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid status in history: " + statusStr);
                }
            }

            // Convert timestamp from Firebase Timestamp to Date
            Date timestamp = null;
            Object timestampObj = map.get("timestamp");
            if (timestampObj instanceof com.google.firebase.Timestamp) {
                timestamp = ((com.google.firebase.Timestamp) timestampObj).toDate();
            } else if (timestampObj instanceof Date) {
                timestamp = (Date) timestampObj;
            }

            StatusHistory history = new StatusHistory(
                    status,
                    timestamp,
                    (String) map.get("note"));
            statusHistory.add(history);
        }
        return statusHistory;
    }
}