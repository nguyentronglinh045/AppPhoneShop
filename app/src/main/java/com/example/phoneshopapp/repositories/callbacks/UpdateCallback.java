package com.example.phoneshopapp.repositories.callbacks;

public interface UpdateCallback {
    void onSuccess();
    void onError(String errorMessage);
}