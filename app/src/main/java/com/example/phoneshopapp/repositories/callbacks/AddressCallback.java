package com.example.phoneshopapp.repositories.callbacks;

import com.example.phoneshopapp.models.Address;

public interface AddressCallback {
    void onSuccess(Address address);
    void onError(String errorMessage);
}