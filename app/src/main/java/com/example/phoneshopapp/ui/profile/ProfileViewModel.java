package com.example.phoneshopapp.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProfileViewModel extends ViewModel {

  private final MutableLiveData<String> userName;
  private final MutableLiveData<String> userEmail;

  public ProfileViewModel() {
    userName = new MutableLiveData<>();
    userEmail = new MutableLiveData<>();

    // Set default user info
    userName.setValue("Truc Nguyen");
    userEmail.setValue("truc@phoneshop.com");
  }

  public LiveData<String> getUserName() {
    return userName;
  }

  public LiveData<String> getUserEmail() {
    return userEmail;
  }

  public void updateUserInfo(String name, String email) {
    userName.setValue(name);
    userEmail.setValue(email);
  }
}