package com.example.phoneshopapp.repositories.callbacks;

import com.example.phoneshopapp.models.Order;

public interface OrderCreationCallback {
    void onSuccess(Order order);
    void onError(String errorMessage);
}