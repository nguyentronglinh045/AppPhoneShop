package com.example.phoneshopapp.repositories.callbacks;

import com.example.phoneshopapp.models.Address;
import java.util.List;

public interface AddressListCallback {
    void onSuccess(List<Address> addresses);
    void onError(String error);
}