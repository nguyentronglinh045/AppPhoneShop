package com.example.phoneshopapp.managers;

import android.content.Context;
import android.util.Log;

import com.example.phoneshopapp.models.Order;
import com.example.phoneshopapp.models.PaymentInfo;
import com.example.phoneshopapp.models.PaymentMethod;
import com.example.phoneshopapp.models.PaymentStatus;
import com.example.phoneshopapp.repositories.callbacks.PaymentCallback;

import java.util.Date;
import java.util.Random;

/**
 * Singleton class xử lý thanh toán giả lập
 * Mock payment processing for learning purposes
 */
public class PaymentManager {
    private static final String TAG = "PaymentManager";
    
    private static PaymentManager instance;
    private final Context context;
    private final Random random;

    private PaymentManager(Context context) {
        this.context = context.getApplicationContext();
        this.random = new Random();
    }

    public static synchronized PaymentManager getInstance(Context context) {
        if (instance == null) {
            instance = new PaymentManager(context);
        }
        return instance;
    }

    /**
     * Process COD (Cash on Delivery) payment
     * @param order Order to process payment for
     * @param callback Callback for success/error handling
     */
    public void processCODPayment(Order order, PaymentCallback callback) {
        Log.d(TAG, "Processing COD payment for order: " + order.getOrderId());
        
        if (!validatePaymentInfo(order.getPaymentInfo())) {
            callback.onError("Thông tin thanh toán không hợp lệ");
            return;
        }

        // Simulate processing delay
        new Thread(() -> {
            try {
                Thread.sleep(1000); // 1 second delay
                
                // COD is always successful (no actual payment processing)
                PaymentInfo updatedPaymentInfo = order.getPaymentInfo();
                updatedPaymentInfo.setStatus(PaymentStatus.PENDING); // COD remains pending until delivery
                updatedPaymentInfo.setTransactionId("COD_" + order.getOrderId());
                
                Log.d(TAG, "COD payment processed successfully");
                callback.onSuccess(updatedPaymentInfo);
                
            } catch (InterruptedException e) {
                Log.e(TAG, "COD payment processing interrupted", e);
                callback.onError("Lỗi xử lý thanh toán COD");
            }
        }).start();
    }

    /**
     * Process bank transfer payment (mock)
     * @param order Order to process payment for
     * @param callback Callback for success/error handling
     */
    public void processBankTransfer(Order order, PaymentCallback callback) {
        Log.d(TAG, "Processing bank transfer payment for order: " + order.getOrderId());
        
        if (!validatePaymentInfo(order.getPaymentInfo())) {
            callback.onError("Thông tin thanh toán không hợp lệ");
            return;
        }

        // Simulate processing delay and random success/failure
        new Thread(() -> {
            try {
                Thread.sleep(2000); // 2 seconds delay
                
                // 90% success rate for mock
                boolean success = random.nextFloat() < 0.9f;
                
                PaymentInfo updatedPaymentInfo = order.getPaymentInfo();
                
                if (success) {
                    updatedPaymentInfo.setStatus(PaymentStatus.PAID);
                    updatedPaymentInfo.setPaidAt(new Date());
                    updatedPaymentInfo.setTransactionId("BANK_" + generateTransactionId());
                    
                    Log.d(TAG, "Bank transfer payment successful");
                    callback.onSuccess(updatedPaymentInfo);
                } else {
                    updatedPaymentInfo.setStatus(PaymentStatus.FAILED);
                    
                    Log.w(TAG, "Bank transfer payment failed");
                    callback.onError("Giao dịch chuyển khoản thất bại. Vui lòng thử lại sau.");
                }
                
            } catch (InterruptedException e) {
                Log.e(TAG, "Bank transfer processing interrupted", e);
                callback.onError("Lỗi xử lý thanh toán chuyển khoản");
            }
        }).start();
    }

    /**
     * Process e-wallet payment (mock)
     * @param order Order to process payment for
     * @param callback Callback for success/error handling
     */
    public void processEWalletPayment(Order order, PaymentCallback callback) {
        Log.d(TAG, "Processing e-wallet payment for order: " + order.getOrderId());
        
        if (!validatePaymentInfo(order.getPaymentInfo())) {
            callback.onError("Thông tin thanh toán không hợp lệ");
            return;
        }

        // Simulate processing delay and random success/failure
        new Thread(() -> {
            try {
                Thread.sleep(1500); // 1.5 seconds delay
                
                // 95% success rate for mock e-wallet
                boolean success = random.nextFloat() < 0.95f;
                
                PaymentInfo updatedPaymentInfo = order.getPaymentInfo();
                
                if (success) {
                    updatedPaymentInfo.setStatus(PaymentStatus.PAID);
                    updatedPaymentInfo.setPaidAt(new Date());
                    updatedPaymentInfo.setTransactionId("EWALLET_" + generateTransactionId());
                    
                    Log.d(TAG, "E-wallet payment successful");
                    callback.onSuccess(updatedPaymentInfo);
                } else {
                    updatedPaymentInfo.setStatus(PaymentStatus.FAILED);
                    
                    Log.w(TAG, "E-wallet payment failed");
                    callback.onError("Thanh toán ví điện tử thất bại. Vui lòng kiểm tra số dư và thử lại.");
                }
                
            } catch (InterruptedException e) {
                Log.e(TAG, "E-wallet processing interrupted", e);
                callback.onError("Lỗi xử lý thanh toán ví điện tử");
            }
        }).start();
    }

    /**
     * Validate payment information
     * @param paymentInfo Payment info to validate
     * @return true if valid, false otherwise
     */
    public boolean validatePaymentInfo(PaymentInfo paymentInfo) {
        if (paymentInfo == null) {
            Log.w(TAG, "Payment info is null");
            return false;
        }

        if (paymentInfo.getMethod() == null) {
            Log.w(TAG, "Payment method is null");
            return false;
        }

        // Additional validation based on payment method
        switch (paymentInfo.getMethod()) {
            case COD:
                // COD doesn't require additional validation
                return true;
                
            case BANK_TRANSFER:
                // In real app, would validate bank account details
                return true;
                
            case EWALLET:
                // In real app, would validate e-wallet account
                return true;
                
            default:
                Log.w(TAG, "Unknown payment method: " + paymentInfo.getMethod());
                return false;
        }
    }

    /**
     * Process payment based on method
     * @param order Order to process payment for
     * @param callback Callback for success/error handling
     */
    public void processPayment(Order order, PaymentCallback callback) {
        if (order == null || order.getPaymentInfo() == null) {
            callback.onError("Thông tin đơn hàng hoặc thanh toán không hợp lệ");
            return;
        }

        PaymentMethod method = order.getPaymentInfo().getMethod();
        
        switch (method) {
            case COD:
                processCODPayment(order, callback);
                break;
                
            case BANK_TRANSFER:
                processBankTransfer(order, callback);
                break;
                
            case EWALLET:
                processEWalletPayment(order, callback);
                break;
                
            default:
                callback.onError("Phương thức thanh toán không được hỗ trợ");
                break;
        }
    }

    /**
     * Check if payment method requires immediate processing
     * @param paymentMethod Payment method to check
     * @return true if requires immediate processing
     */
    public boolean requiresImmediateProcessing(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.BANK_TRANSFER || 
               paymentMethod == PaymentMethod.EWALLET;
    }

    /**
     * Get payment method display info
     * @param paymentMethod Payment method
     * @return Display information string
     */
    public String getPaymentMethodInfo(PaymentMethod paymentMethod) {
        switch (paymentMethod) {
            case COD:
                return "Thanh toán khi nhận hàng - Không cần thanh toán trước";
                
            case BANK_TRANSFER:
                return "Chuyển khoản ngân hàng - Thanh toán ngay lập tức";
                
            case EWALLET:
                return "Ví điện tử - Thanh toán an toàn và nhanh chóng";
                
            default:
                return "Phương thức thanh toán không xác định";
        }
    }

    // Helper methods

    private String generateTransactionId() {
        long timestamp = System.currentTimeMillis();
        int randomNum = random.nextInt(10000);
        return String.valueOf(timestamp).substring(6) + String.format("%04d", randomNum);
    }

    /**
     * Simulate refund processing (for cancelled orders)
     * @param order Order to refund
     * @param callback Callback for success/error handling
     */
    public void processRefund(Order order, PaymentCallback callback) {
        if (order == null || order.getPaymentInfo() == null) {
            callback.onError("Thông tin đơn hàng không hợp lệ");
            return;
        }

        PaymentInfo paymentInfo = order.getPaymentInfo();
        
        // Only process refund if payment was already made
        if (paymentInfo.getStatus() != PaymentStatus.PAID) {
            callback.onError("Đơn hàng chưa được thanh toán, không cần hoàn tiền");
            return;
        }

        // COD orders don't need refund
        if (paymentInfo.getMethod() == PaymentMethod.COD) {
            callback.onError("Đơn hàng COD không cần hoàn tiền");
            return;
        }

        Log.d(TAG, "Processing refund for order: " + order.getOrderId());

        // Simulate refund processing
        new Thread(() -> {
            try {
                Thread.sleep(2000); // 2 seconds delay
                
                // Refund is always successful in mock
                PaymentInfo updatedPaymentInfo = order.getPaymentInfo();
                updatedPaymentInfo.setTransactionId("REFUND_" + updatedPaymentInfo.getTransactionId());
                
                Log.d(TAG, "Refund processed successfully");
                callback.onSuccess(updatedPaymentInfo);
                
            } catch (InterruptedException e) {
                Log.e(TAG, "Refund processing interrupted", e);
                callback.onError("Lỗi xử lý hoàn tiền");
            }
        }).start();
    }
}