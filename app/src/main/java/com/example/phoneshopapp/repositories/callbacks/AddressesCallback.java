package com.example.phoneshopapp.repositories.callbacks;

import com.example.phoneshopapp.models.Address;
import java.util.List;

public interface AddressesCallback {
    void onSuccess(List<Address> addresses);
    void onError(String errorMessage);
}