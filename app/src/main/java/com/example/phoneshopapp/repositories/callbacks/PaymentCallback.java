package com.example.phoneshopapp.repositories.callbacks;

import com.example.phoneshopapp.models.PaymentInfo;

public interface PaymentCallback {
    void onSuccess(PaymentInfo paymentInfo);
    void onError(String errorMessage);
}