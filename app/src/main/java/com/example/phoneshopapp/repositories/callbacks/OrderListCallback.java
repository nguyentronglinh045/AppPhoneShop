package com.example.phoneshopapp.repositories.callbacks;

import com.example.phoneshopapp.models.Order;
import java.util.List;

public interface OrderListCallback {
    void onSuccess(List<Order> orders);
    void onError(String error);
}